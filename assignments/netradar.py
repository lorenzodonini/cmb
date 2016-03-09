
from itertools import repeat

# wifi_tech = '0'
# wdca_tech = '8'
# gsm_tech = '15'
# lte_tech = '13'
# cellular_technologies = [wdca_tech, lte_tech, gsm_tech]


def read_csv(file_name, separator):
    file = open(file_name, "r")
    rows = file.read().split("\n")
    if len(rows) < 2:
        return None

    header = rows[0].split(separator)
    rows = rows[1:]
    document = []

    for row in rows:
        if not row.strip():
            continue

        cells = row.split(separator)
        doc_row = {}
        for col, cell in enumerate(cells):
            doc_row[header[col]] = cell

        document.append(doc_row)
    return document


def get_hour_from_str(time_str):
    if time_str == '-':
        return None
    return int(time_str.split(' ')[1].split(':')[0])


def get_row_hour(row):
    if "ping_1_time" not in row:
        return None
    return get_hour_from_str(row["ping_1_time"])


def make_key(prefix, index):
    return prefix + "_" + str(index) + "_value"


def get_values_by_prefix(row, prefix, keep_none_values=False):
    values = []

    index = 1
    while make_key(prefix, index) in row:
        value = row[make_key(prefix, index)]
        index += 1
        if value is not None and value != "" and value != "-":
            values.append(int(value))
        elif keep_none_values:
            values.append(None)

    return values


def calculate_mean(values):
    if not values:
        return float('nan')
    return sum(values) / len(values)


def evolution_of_metrics(doc, prefix, wifi):
    width = len(get_values_by_prefix(doc[0], prefix, True))
    totals = list(repeat(0, width))
    counts = list(repeat(0, width))

    for row in doc:
        if row_is_wifi(row) != wifi:
            continue

        measurement = get_values_by_prefix(row, prefix, True)
        for index, value in enumerate(measurement):
            if value is not None:
                totals[index] += value
                counts[index] += 1

    for index, count in enumerate(counts):
        if count > 0:
            totals[index] /= count

    return totals


def row_is_wifi(row):
    return row["radiotech"] == '0'


def min_of(doc, prefix, wifi):
    res = None
    for row in doc:
        if row_is_wifi(row) != wifi:
            continue

        for value in get_values_by_prefix(row, prefix):
            if (res is None or value < res) and value > 0:
                res = value

    return res


def max_of(doc, prefix, wifi):
    res = None
    for row in doc:
        if row_is_wifi(row) != wifi:
            continue
        for value in get_values_by_prefix(row, prefix):
            if res is None or value > res:
                res = value

    return res

def average_of(doc, prefix, wifi):
    total = 0
    count = 0
    for row in doc:
        if row_is_wifi(row) != wifi:
            continue
        for value in get_values_by_prefix(row, prefix)[-100:]:  # get last 100 samples of measurement
            if value != 0:                                      # ignore 0 values
                total += value
                count += 1

    return total / count


def average_by_hour(doc, prefix, wifi):
    totals = list(repeat(0, 24))
    counts = list(repeat(0, 24))

    for row in doc:
        if row_is_wifi(row) != wifi:
            continue
        hour_index = get_row_hour(row)
        if hour_index is None:
            continue

        for value in get_values_by_prefix(row, prefix)[-100:]:  # get last 100 samples of measurement
            if value != 0:                                      # ignore 0 values
                totals[hour_index] += value
                counts[hour_index] += 1
    for index, count in enumerate(counts):
        if count > 0:
            totals[index] /= count

    return totals


def run_test(function, doc):
    print function.__name__, "download wifi", function(doc, "dlsample", True)
    print function.__name__, "download cellular", function(doc, "dlsample", False)
    print function.__name__, "upload wifi", function(doc, "ulsample", True)
    print function.__name__, "upload cellular", function(doc, "ulsample", False)
    print function.__name__, "ping wifi", function(doc, "ping", True)
    print function.__name__, "ping cellular", function(doc, "ping", False)


doc = read_csv("netradar.team4", ",")


run_test(max_of, doc)
run_test(min_of, doc)
run_test(average_of, doc)
run_test(evolution_of_metrics, doc)
run_test(average_by_hour, doc)
