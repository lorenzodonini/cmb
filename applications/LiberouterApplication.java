package applications;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;

import core.Application;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.Connection;
import routing.MessageRouter;
import routing.ActiveRouter;
import routing.SprayAndWaitRouter;


public class LiberouterApplication extends Application {

	public static final String APP_ID = "fi.tkk.netlab.LiberouterApplication";
	public static final String LEGACY_MSG = APP_ID + ".legacyMessage";
	public static final int    NORMAL_MSG = 0;
	public static final int    COOKIE_MSG = 1;
	public static final int    OBJECT_MSG = 2;

	private boolean log = false;
	private int appNo = 0;
	private HashMap<String,LinkedList<Message>>  lr_queues = new HashMap ();

	public LiberouterApplication(Settings s) {
		//if (s.contains(MAX_COOKIE_SIZE)){
		//    this.max_cookie_Size = s.getInt(MAX_COOKIE_SIZE);
		//}
		if (log) System.out.println("Initializing " + APP_ID);
		super.setAppID (APP_ID);
	}

	public LiberouterApplication(LiberouterApplication app) {
		super(app);
		this.appNo = app.getNo()+1;
		// TODO Auto-generated constructor stub
	}

	@Override
	public Message handle(Message msg, DTNHost host) {
		// TODO Auto-generated method stub
		// System.out.println("Message "+ msg.getId()+ " arrived to node " + host.toString() + " appID " + this.appID + " # " + appNo +" param.x "+ this.x);
		return msg;
	}

	@Override
	public void update(DTNHost host) {
		// System.out.println("Update called in " + host.toString());
		// TODO Auto-generated method stub

	}

	@Override
	public Application replicate() {
		// TODO Auto-generated method stub
		return new LiberouterApplication(this);
	}

	public Collection<Message> initiateTransfer (DTNHost my_host, DTNHost remote_host, Collection<Message> remote_msgs, Connection conn,
												 int max_cookie_size, int max_object_size, boolean send_cookies, boolean send_msgs) {
		Collection<Message>    my_msgs = null;
		Collection<Message>    get_msgs = new LinkedList<Message> ();
		boolean                add;
		LinkedList<Message>    q = null;
		int                    size_to_send = 0;
		Message                m;
		ActiveRouter           r;
		boolean                have_cookies = false;
		boolean                snw_routing = my_host.getRouter () instanceof SprayAndWaitRouter;

		// how do I get access to my local router?
		// Ok, I'll pass the host as a parameter from the remote router module, then it is available here --> really ugly
		if (log) System.out.println("[" + remote_host + "]\tinitiateTransfer (liberouter: " + my_host.toString() + " legacy node: " + remote_host + ")");
		my_msgs = my_host.getRouter ().getMessageCollection ();

		if (log) System.out.println ("[" + remote_host + "]\tLiberouter " + my_host.toString () + "   : " + my_msgs);
		for (Message my_m : my_msgs) {
			add = true;
			for (Message remote_m : remote_msgs) {
				// System.out.println ("...comparing: " + my_m.toString () + " == " + remote_m.toString ());
				if (my_m.compareTo (remote_m) == 0) {
					add = false;
					break;
				}
				if (snw_routing) {
		    /* special treatment as we only want to spread messages of which we have more than a single copy left */
					Integer nrofCopies = (Integer) my_m.getProperty (SprayAndWaitRouter.MSG_COUNT_PROPERTY);

					if (nrofCopies == 1) {
						add = false;
						break;
					}
				}
			}
	    /* only consider messages that fit into a storage object */
			if (add && my_m.getSize () <= max_object_size) {
		/* first, check if we need a new queue */
				if (q == null) {
					if ((q = lr_queues.get (remote_host.toString())) == null) {
						q = new LinkedList<Message> ();
						lr_queues.put (remote_host.toString (), q);
					}
				}
		/*
		 * Enqueue the message locally, but don't set the 'comes from Liberouter app property yet, and enqueue the message
		 * We copy the entire message rather than just the ID so that we have the size information for later operation.
		 * Since cookies shall be sent first, we put them to the beginning of the queue.  Assuming that this will fast
		 * anyway, we don't do much reorg here.
		 */
				if (my_m.getSize () <= max_cookie_size) {
					q.addFirst (my_m.replicate ());
					have_cookies = true;
				} else
					q.addLast (my_m.replicate ());
			}
		}
		// TODO: here we should re-organize the messages if we did not take care of this during the dequeuing process
		// now schedule the first message for delivery
		if (q != null) {
	    /* we remembered a reference to the original message earlier, so we can manipulate the message in the queue here */
			if (log) System.out.println ("[" + remote_host + "]\t         LR.send = " + q);
	    /* only start sending if we are told so -- i.e., if there are no cookies to receive from the legacy node */
			if ((send_cookies && have_cookies) || send_msgs) {
				m = q.remove ();
				if (log) System.out.println ("[" + remote_host + "]\tInvoking forwardMessage: " + m.toString ());
				m.updateProperty (LEGACY_MSG, m.getSize () <= max_cookie_size ? COOKIE_MSG : OBJECT_MSG);
				r = (ActiveRouter) my_host.getRouter ();
				if (r.forwardMessage (m, conn) != MessageRouter.RCV_OK) {
					if (log) System.out.println ("[" + remote_host + "]\tCANNOT initiate forwarding");
					q.addFirst (m);
				}
				m.updateProperty (LEGACY_MSG, NORMAL_MSG);
		/* remove the queue if this was the last message */
				if (q.size () == 0) {
					lr_queues.remove (remote_host.toString ());
				}
			}
		}
	/* eventually, we should return the list of message to be sent by the router (but let's get one direction working first) */
		for (Message remote_m : remote_msgs) {
	    /* we can cookies anyway, so only ask for web objects (which should fit by default, but we check anyway */
			if (remote_m.getSize () > max_cookie_size) {
				add = true;
				for (Message my_m : my_msgs) {
					if (my_m.compareTo (remote_m) == 0) {
						add = false;
						break;
					}
				}
				if (add)
					get_msgs.add (remote_m);
			}
		}
		if (log) System.out.println ("[" + remote_host + "]\t         LR.recv = " + get_msgs);
		return get_msgs;
	}

	public int nextTransfer (DTNHost my_host, DTNHost remote_host, Connection conn, int max_cookie_size, int max_object_size) {
		LinkedList<Message>  q = lr_queues.get (remote_host.toString());
		Message              m = null;
		ActiveRouter         r;
		int                  ret;

		if (log) System.out.println("[" + remote_host + "]\tnextTransfer (liberouter: " + my_host.toString() + " legacy node: " + remote_host + ")");
		if (q != null) {
	    /* we remember a pointer to the original message earlier, so we can manipulate the message in the queue here */
			if (log) System.out.println ("[" + remote_host + "]\t   LR.send = " + q);
			if ((m = q.remove ()) != null) {
				if (log) System.out.println ("[" + remote_host + "]\tInvoking forwardMessage: " + m.toString ());
				m.updateProperty (LEGACY_MSG, m.getSize () <= max_cookie_size ? COOKIE_MSG : OBJECT_MSG);
				r = (ActiveRouter) my_host.getRouter ();
				if ((ret = r.forwardMessage (m, conn)) != MessageRouter.RCV_OK) {
					if (log) System.out.println ("[" + remote_host + "]\tCANNOT initiate forwarding: " + ret);
					q.addFirst (m);
					return 0;
				}
		/* sendMessage () will create a copy of the message -> now we reset the flag */
				m.updateProperty (LEGACY_MSG, NORMAL_MSG);
		/* remove the queue if this was the last message */
				if (q.size () == 0) {
					lr_queues.remove (remote_host.toString ());
				}
				return 1;
			}
		}
		return -1;
	}

	public int cancelTransfer (DTNHost my_host, DTNHost remote_host, Connection conn) {
		LinkedList<Message>  q = lr_queues.get (remote_host.toString());

		if (log) System.out.println("[" + remote_host + "]\tcancelTransfer (liberouter: " + my_host.toString() + " legacy node: " + remote_host + ")");
		if (q != null) {
			q.clear ();
			lr_queues.remove (remote_host.toString ());
		}
		return 0;
	}

	public int getNo(){
		return this.appNo;
	}

}
