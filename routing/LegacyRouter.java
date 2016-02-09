/* 
 * Copyright 2014 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import core.Connection;
import core.Settings;
import core.Application;
import core.Message;
import core.DTNHost;
import core.SimError;
import core.SimClock;
import applications.LiberouterApplication;

/**
 * Passive router that doesn't send anything unless commanded. This is useful
 * for external event -controlled routing or dummy nodes.
 * For implementation specifics, see MessageRouter class.
 */
public class LegacyRouter extends ActiveRouter {
    public final static String LEGACY_ROUTER_NS = "LegacyRouter";
    public final static String COOKIE_N         = "nrofCookies";
    public final static String COOKIE_SIZE      = "sizePerCookie";
    public final static String COOKIE_STORAGE   = "cookieStorage";
    public final static String WEBOBJ_N         = "nrofObjects";
    public final static String WEBOBJ_SIZE      = "sizePerObject";
    public final static String WEBOBJ_STORAGE   = "objectStorage";
    public final static String REPL_POLICY      = "replPolicy";
    public final static String REPL_POLICY_LEGACY_FIRST_S     = "legacy";
    public final static String REPL_POLICY_LIBEROUTER_FIRST_S = "liberouter";
    public final static String REPL_POLICY_ALTERNATING_S      = "alternating";

    public final static int COOKIES     = 0x01;
    public final static int WEBSTORAGE  = 0x02;

    public final static int REPL_POLICY_LEGACY_FIRST     = 1;
    public final static int REPL_POLICY_LIBEROUTER_FIRST = 2;
    public final static int REPL_POLICY_ALTERNATING      = 3;

    /* variables set by the configuration file */
    private int        repl_policy    = REPL_POLICY_LEGACY_FIRST;
    private int        max_cookies    = 0;
    private int        cookie_size    = 1024;
    private int        cookie_storage = 16*1024;
    private int        max_objects    = 0;
    private int        object_size    = 1024*1024;
    private int        object_storage = 1024*1024*16;

    /* object-specific state variables */
    private Connection current_conn   = null;
    private LiberouterApplication current_app = null;
    private Message    current_msg    = null;
    private LinkedList<Message> msgs_to_send = new LinkedList<Message> ();
    private int        receiving      = 0;
    private boolean    we_send        = true;
    private int        n_cookies      = 0;
    private int        n_objects      = 0;
    private int        size_objects   = 0;

    private boolean    log            = false;

    LinkedList<Message> received_cookies = new LinkedList<Message> ();
    LinkedList<Message> received_objects = new LinkedList<Message> ();

    public LegacyRouter(Settings s) {
        super(s);
        Settings   lr_settings = new Settings (LEGACY_ROUTER_NS);
        int        buffer_needed = 0;

        if (lr_settings.contains (COOKIE_N)) {
            max_cookies = lr_settings.getInt (COOKIE_N);
        }
        if (lr_settings.contains (COOKIE_SIZE)) {
            cookie_size = lr_settings.getInt (COOKIE_SIZE);
        }
        if (lr_settings.contains (COOKIE_STORAGE)) {
            cookie_storage = lr_settings.getInt (COOKIE_STORAGE);
        }
        if (lr_settings.contains (WEBOBJ_N)) {
            max_objects = lr_settings.getInt (WEBOBJ_N);
        }
        if (lr_settings.contains (WEBOBJ_SIZE)) {
            object_size = lr_settings.getInt (WEBOBJ_SIZE);
        }
        if (lr_settings.contains (WEBOBJ_STORAGE)) {
            object_storage = lr_settings.getInt (WEBOBJ_STORAGE);
        }
        if (lr_settings.contains (WEBOBJ_STORAGE)) {
            object_storage = lr_settings.getInt (WEBOBJ_STORAGE);
        }
        if (lr_settings.contains (REPL_POLICY)) {
            if (lr_settings.getSetting (REPL_POLICY).equals (REPL_POLICY_LEGACY_FIRST_S))
                repl_policy = REPL_POLICY_LEGACY_FIRST;
            else if (lr_settings.getSetting (REPL_POLICY).equals (REPL_POLICY_LIBEROUTER_FIRST_S))
                repl_policy = REPL_POLICY_LIBEROUTER_FIRST;
            else if (lr_settings.getSetting (REPL_POLICY).equals (REPL_POLICY_ALTERNATING_S))
                repl_policy = REPL_POLICY_ALTERNATING;
            else {
                throw new SimError("Unknown LegacyRouter.replPolicy: " + lr_settings.getSetting (REPL_POLICY));
            }
        }

        buffer_needed  = max_cookies * cookie_size > cookie_storage ? max_cookies * cookie_size : cookie_storage;
        buffer_needed += max_objects * object_size > object_storage ? max_objects * object_size : object_storage;
        buffer_needed *= 2;
        if (buffer_needed > getBufferSize ()) {
            throw new SimError("Buffer size too small for legacy nodes: need 2 * (cookies + web objects) = " + buffer_needed);
        }
        if (cookie_size > object_size) {
            throw new SimError("Max cookie size (" + cookie_size + ") > max object_size (" + object_size + ")");
        }
    }

    /**
     * Copy-constructor.
     * @param r Router to copy the settings from.
     */
    protected LegacyRouter(LegacyRouter r) {
        super(r);
        max_cookies    = r.max_cookies;
        cookie_size    = r.cookie_size;
        cookie_storage = r.cookie_storage;
        max_objects    = r.max_objects;
        object_size    = r.object_size;
        object_storage = r.object_storage;
        repl_policy    = r.repl_policy;
    }

    @Override
    public void update() {
        int    ret;

        super.update();
        if (current_conn != null) {
	    /*
	     * send the next message if any:
	     * -- send always as long as we have cookies
	     * -- otherwise send the next message only if its our turn
	     */
            if ((current_msg == null && msgs_to_send.size () > 0) &&
                    (msgs_to_send.peek ().getSize () <= cookie_size || we_send || receiving == -1)) {
                LinkedList<Message>  to_delete = new LinkedList<Message> ();

                for (Message m : msgs_to_send) {
                    if (log) System.out.println ("[" + getHost () + "]\ttrying msg " + m.toString ());
                    ret = startTransfer (m, current_conn);
                    if (ret > 0) {
			/* busy -> no point in trying further messages */
                        break;
                    } else if (ret == RCV_OK) {
                        current_msg = m;
                        break;
                    } else if (ret == DENIED_OLD) {
			/* don't keep messages hanging around need anymore
			 * those may be cookies or messages the liberouter received in the meantime from another node
			 */
                        if (log) System.out.println ("[" + getHost () + "]\tremoving message " + m.toString ());
                        to_delete.add (m);
                    }
                }
                for (Message m : to_delete)
                    msgs_to_send.remove (m);
                to_delete.clear ();
                // current_msg = tryAllMessages (current_conn, msgs_to_send);
                if (current_msg != null) {
                    if (log) System.out.println ("[" + getHost () + "]\tlegacy sending [next]: " + current_msg + " size=" + current_msg.getSize ());
                    if (current_msg.getSize () > cookie_size && repl_policy == REPL_POLICY_ALTERNATING) {
			/* sending a web object -> next turn is the liberouter */
                        if (log) System.out.println ("[" + getHost () + "]\talternating... liberouter is next");
                        we_send = false;
                    }
                }
            }
	    /* initiate receiving the next message if any -> works only if we are not sending */
            if (current_msg == null && receiving == 0) {
                if (log) System.out.println ("[" + getHost () + "]\tcalling nextTransfer at " + current_conn.getOtherNode (getHost ()));
                receiving = current_app.nextTransfer (current_conn.getOtherNode (getHost ()), getHost (), current_conn, cookie_size, object_size);
		/* in case of alternating, it's our turn again provided that there still something left to send */
                if (receiving == 1 && repl_policy == REPL_POLICY_ALTERNATING && msgs_to_send.size () > 0) {
                    if (log) System.out.println ("[" + getHost () + "]\talternating... legacy node is next");
                    we_send = true;
                }
            }
        }
    }

    @Override
    public int receiveMessage(Message m, DTNHost from) {
        Integer     type;

        if ((type = (Integer) m.getProperty (LiberouterApplication.LEGACY_MSG)) != null) {
            if (type != LiberouterApplication.NORMAL_MSG) {
                if (log) System.out.print ("[" + getHost () + "]\treceiveMessage: " + from.toString () + " -> " + getHost () + " msg " + m.toString ());
                if (log) System.out.println (" -> ACCEPTED");
                return super.receiveMessage (m, from);
            }
        }
        // System.out.println (" -> DENIED");
        return DENIED_UNSPECIFIED;
    }

    @Override
    public void changedConnection(Connection conn) {
        Collection<Application>   capp;
        Collection<Message>       my_msgs;
        Message                   m2;

        // We have a new connection: if this connection is to a liberouter, initiate
        // transfer of our own messages and make the remote liberouter application send
        // messages to us.
        // The interaction which messages to transmit when is quite complex and partly governed by the REPL_POLICY
        // Irrespective of the policy:
        // 1. The legacy node sends its cookies first (as they would go into the first HTTP request anyway)
        // 2. The liberouter then sends its cookies (as they would be included in the first HTTP response)
        // Then the REPL_POLICY sets in.
        //
        // A liberouter is characterized by having an application running that accepts liberouter messages
        // System.out.println("changedConnection event: " + conn.toString ());
        if (conn.isUp ()) {
	    /* Let's make sure that we can talk only to one AP at a time */
            if (current_conn == null) {
                capp = conn.getOtherNode(getHost()).getRouter().getApplications (LiberouterApplication.APP_ID);
                if (capp != null) {
		    /* iterate through capp to check if we have a liberouter application */
                    for (Application app : capp) {
                        if (app.getAppID ().equals (LiberouterApplication.APP_ID)) {
                            if (log) System.out.println ("[" + getHost () + "]\t----------------------------------------------------------------------");
			    /* we set this only when there is a liberouter application */
                            current_conn = conn;
                            current_app  = (LiberouterApplication) app;
                            // System.out.println("met liberouter");
                            my_msgs = getMessageCollection ();
                            if (log) System.out.println ("[" + getHost () + "]\tLegacy Node " + getHost ().toString () + "\t : " + my_msgs);
			    /* we send all cookies in the beginning by default */
                            for (Message m : my_msgs) {
                                if (m.getSize () <= cookie_size)
                                    msgs_to_send.add (m);
                            }
                            if (log) System.out.println ("[" + getHost () + "]\t\tcookies\t : " + msgs_to_send);
			    /* the liberouter application decides which web objects we get and which it wants */
                            msgs_to_send.addAll (msgs_to_send.size (),
                                    current_app.initiateTransfer (conn.getOtherNode (getHost ()), getHost (), my_msgs, conn,
                                            cookie_size, object_size,  /* what's a cookie and what's a web object */
                                            msgs_to_send.size () == 0, /*receive cookies if we don't have any to send */
									       /* receive messages if we don't have cookies and liberouter goes first */
                                            msgs_to_send.size () == 0 && repl_policy == REPL_POLICY_LIBEROUTER_FIRST));
                            if (log) System.out.println ("[" + getHost () + "]\tlegacy send queue: " + msgs_to_send);
                            current_msg = tryAllMessages (conn, msgs_to_send);

			    /* define whose turn it is next as a function of the policy */
                            we_send = (repl_policy == REPL_POLICY_LEGACY_FIRST) || (repl_policy == REPL_POLICY_ALTERNATING);

                            if (current_msg != null) {
                                if (log) System.out.println ("[" + getHost () + "]\tlegacy sending [init]: " + current_msg + " size=" + current_msg.getSize ());
				/* update who sends if we already sent a full message rather than just a cookie */
                                if (current_msg.getSize () > cookie_size) {
                                    if (log) System.out.println ("[" + getHost () + "]\talternating... liberouter is next");
                                    we_send = (repl_policy == REPL_POLICY_LEGACY_FIRST);
                                }
                            }
			    /* we only work with a single liberouter application */
                            break;
                            // NOTE:
                            // Can we get into a situation where we have only messages left the other side doesn't want any of our messages
                            // and then blocks on transmitting its own messages?
                            // We may have to try the messages individually to find out.  But we can do this in update() and not here.
                        }
                    }
                }
            }
        } else {
	    /* connection went down -> let's terminate all transfers, clear all queues, decide which cookies and objects to keep */
            if (current_conn == conn) {
                LinkedList<Message>   ml = new LinkedList<Message> ();

		/* maybe cancel the remaining transfers */
                current_app.cancelTransfer (conn.getOtherNode (getHost ()), getHost (), conn);
                current_app  = null;
                current_conn = null;
                current_msg  = null;
                msgs_to_send.clear ();
                receiving    = 0;
                we_send      = true;
		/* Now is the time for book-keeping.
		 * We may have gotten more web objects than we are willing to store -> clean up
		 */
                if (log) System.out.println ("Exchange done, limit check. Cookies: " + n_cookies + "/" + max_cookies +
                        " Objects: " + n_objects + "/" + max_objects + " size: " + size_objects + "/" + object_storage);
                if (n_cookies > max_cookies) {
		    /*
		     * all the cookie messages we have
		     * recalculate n_cookies as some messages may have expired in the meantime and the ActiveRouter won't tell us
		     */
                    n_cookies = 0;
                    for (Message m : getMessageCollection ()) {
                        if (m.getSize () <= cookie_size) {
                            ml.add (m);
                            n_cookies++;
                        }
                    }
                    if (log) System.out.println ("     validated limit check. Cookies: " + n_cookies + "/" + max_cookies);
		    /* try to keep the ones we just received */
                    for (Message m : received_cookies)
                        ml.remove (m);
                    Collections.shuffle (ml, new Random (SimClock.getIntTime()));
                    while (ml.size () > 0 && n_cookies > max_cookies) {
                        Message   m = ml.remove ();
                        if (log) System.out.println ("[" + getHost () + "]\tdeleting old cookie " + m + " size=" + m.getSize ());
			/* double check that this message did not expire and disappear in the meantime */
                        if (hasMessage (m.toString ()))
                            deleteMessage (m.toString (), true);
                        n_cookies--;
                    }
                    if (n_cookies > max_cookies) {
			/* we got more new cookies than would fit --> remove random new ones */
                        Collections.shuffle (received_cookies, new Random (2*SimClock.getIntTime ()));
                        while (received_cookies.size () > 0 && n_cookies > max_cookies) {
                            Message   m = received_cookies.remove ();
                            if (log) System.out.println ("[" + getHost () + "]\tdeleting new cookie " + m + " size=" + m.getSize ());
                            if (hasMessage (m.toString ()))
                                deleteMessage (m.toString (), true);
                            n_cookies--;
                        }
                    }
                    ml.clear ();
                }
                if ((max_objects != 0 && n_objects > max_objects) || size_objects > object_storage) {
		    /*
		     * all the object messages we have
		     * again, validate the object count because of messages may have been timed out
		     */
                    n_objects = 0;
                    size_objects = 0;
                    for (Message m : getMessageCollection ()) {
                        if (m.getSize () > cookie_size) {
                            ml.add (m);
                            n_objects++;
                            size_objects += m.getSize ();
                        }
                    }
                    if (log) System.out.println ("     validated limit check. Objects: " + n_objects + "/" + max_objects + " size: " + size_objects + "/" + object_storage);
		    /* remove the ones we just received */
                    for (Message m : received_objects)
                        ml.remove (m);
                    Collections.shuffle (ml, new Random (3*SimClock.getIntTime()));
                    while (ml.size () > 0 && ((max_objects != 0 && n_objects > max_objects) || size_objects > object_storage)) {
                        Message   m = ml.remove ();
                        if (log) System.out.println ("[" + getHost () + "]\tdeleting old object " + m + " size=" + m.getSize ());
			/* double check that this message did not expire and disappear in the meantime */
                        if (hasMessage (m.toString ()))
                            deleteMessage (m.toString (), true);
                        n_objects--;
                        size_objects -= m.getSize ();
                    }
                    if ((max_objects != 0 && n_objects > max_objects) || size_objects > object_storage) {
			/* we got more new objects than would fit --> remove random new ones */
                        Collections.shuffle (received_objects, new Random (4*SimClock.getIntTime ()));
                        while (received_objects.size () > 0 && ((max_objects != 0 && n_objects > max_objects) || size_objects > object_storage)) {
                            Message   m = received_objects.remove ();
                            if (log) System.out.println ("[" + getHost () + "]\tdeleting new object " + m + " size=" + m.getSize ());
			    /* double check that this message did not expire and disappear in the meantime */
                            if (hasMessage (m.toString ()))
                                deleteMessage (m.toString (), true);
                            n_objects--;
                            size_objects -= m.getSize ();
                        }
                    }
                    ml.clear ();
                }
		/* clear the knowledge from the last transfer */
                received_objects.clear ();
                received_cookies.clear ();
                if (log) System.out.println ("[" + getHost () + "]\t++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            }
        }
    }

    @Override
    protected void transferDone (Connection conn) {
        assert current_conn == conn : "transferDone on wrong connection";
	
	/* One message done -> prepare to send the next one
	 * This is then done (according to the policy) in the update() method
	 */
        msgs_to_send.remove (current_msg);
        current_msg = null;
    }

    @Override
    public Message messageTransferred (String id, DTNHost from) {
        Message m = super.messageTransferred (id, from);

        assert m.getSize () <= object_size : "Got too large an object from a liberouter: " + m.getSize ();
        m.updateProperty (LiberouterApplication.LEGACY_MSG, LiberouterApplication.NORMAL_MSG);
	/* we record what we get from the liberouter to be later on shuffle decide what to keep */
        if (m.getSize () <= cookie_size) {
            if (log) System.out.println ("[" + getHost () + "]\treceived cookie " + m + " size=" + m.getSize ());
            n_cookies++;
            received_cookies.add (m);
        } else {
            if (log) System.out.println ("[" + getHost () + "]\treceived object " + m + " size=" + m.getSize ());
            n_objects++;
            size_objects += m.getSize ();
            received_objects.add (m);
        }
	/* One message done -> prepare to receive the next one
	 * This is scheduled (according to the policy) in the update() method
	 */
        receiving = 0;
        return m;
    }

    /* We do not interact with other nodes except liberouter, hence we cannot allow messages to be 'pulled' */
    @Override
    public boolean requestDeliverableMessages (Connection conn) {
        return false;
    }

    @Override
    public LegacyRouter replicate() {
        return new LegacyRouter(this);
    }
}
