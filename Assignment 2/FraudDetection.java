package dsa;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Collections.swap;

public class FraudDetection {
    /* Function to retrieve median of array in constant time */
    public static double medianOf(ArrayList<Double> arr) {
        int size = arr.size();
        return size % 2 == 0 ? (arr.get((size / 2) - 1) + arr.get(size / 2)) / 2 : arr.get(size / 2);
    }

    /* Function to convert date in integer format, to parse days and inflect skipped days */
    public static LocalDate getDateFromNumber(Integer number) {
        return LocalDate.parse(String.valueOf(number), DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    /* Linear based algorithm that is used to perform stable radix sort to sort transactions by date */
    private static void countingSort(ArrayList<Day> arr, int currentDigit, int size) {
        ArrayList<Day> result = new ArrayList<>();
        int[] count = new int[10];

        for (int z = 0; z < size; z++) {
            result.add(new Day(0, 0));
        }

        for (int i = 0; i < size; i++) {
            count[(arr.get(i).getDate() / currentDigit) % 10]++;
        }

        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }

        for (int i = size - 1; i >= 0; i--) {
            int date = arr.get(i).getDate(), digit = (date / currentDigit) % 10;
            result.set(count[digit] - 1, new Day(date, arr.get(i).getTransaction()));
            count[digit]--;
        }

        for (int i = 0; i < size; i++) {
            arr.set(i, result.get(i));
        }
    }


    public static void radixSort(ArrayList<Day> arr, int size) {
        for (int digit = 1; digit < Math.pow(10, 8); digit *= 10) {
            countingSort(arr, digit, size);
        }
    }

    private static int partition(ArrayList<Double> arr, int low, int high) {
        double key = arr.get(high);
        int i = low - 1;
        for (int k = low; k < high; k++) {
            if (arr.get(k) < key) {
                swap(arr, ++i, k);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    /* Comparison based algorithm to sort array, taken from queue of trailing days */
    public static void quickSort(ArrayList<Double> arr, int low, int high) {
        if (low < high) {
            int key = partition(arr, low, high);
            quickSort(arr, low, key - 1);
            quickSort(arr, key + 1, high);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        ArrayList<Day> expenses = new ArrayList<>();

        boolean sameDay = false;
        double transaction, temporarySum;
        int skippedDays, currentDay, alerts = 0, n = scanner.nextInt(), d = scanner.nextInt();

        /* Processing input data, by adding it in ArrayList of days */
        for (int i = 0; i <= n; i++) {
            String line = scanner.nextLine();
            if (Objects.equals(line, "")) continue;

            String[] lines = line.split(" ");
            transaction = Double.parseDouble(lines[1].replaceFirst("\\$", ""));
            currentDay = Integer.parseInt(lines[0].replaceAll("-", ""));
            expenses.add(new Day(currentDay, transaction));
        }

        radixSort(expenses, expenses.size());
        Queue<Double> trailingDays = new LinkedList<>();          // The queue to keep last d trailing days
        ArrayList<Double> days = new ArrayList<>(trailingDays);   // The list in which we will sort data from queue
        ArrayList<Double> transactions = new ArrayList<>();
        expenses.add(new Day(0, 0));   // Adding one dull day to have "i" loops instead of "i - 1" loops

        for(int i = 1; i < expenses.size(); i++) {

            temporarySum = 0;
            Day presentDay = expenses.get(i - 1);
            Day nextDay = expenses.get(i);

            // Retrieving several consecutive days, by checking current and the next day
            if (nextDay.getDate().equals(presentDay.getDate())) {
                if (!sameDay) transactions.clear();
                transactions.add(presentDay.getTransaction());
                sameDay = true;
                if (i != expenses.size() - 1) {
                    continue;
                } else {
                    transactions.add(nextDay.getTransaction());
                }
            } else {
                if (sameDay) {
                    transactions.add(presentDay.getTransaction());
                    sameDay = false;
                } else {
                    transactions.clear();
                    transactions.add(presentDay.getTransaction());
                    if (i == expenses.size()) {
                        transactions.clear();
                        transactions.add(nextDay.getTransaction());
                    }
                }
            }

            // Checking if the consecutive sum of transactions will lead to an alert
            for (double tc : transactions) {
                temporarySum += tc;
                if (trailingDays.size() != d) continue;
                if (temporarySum != 0 && temporarySum >= 2 * medianOf(days)) {
                    alerts++;
                }
            }

            // Updating the queue
            if (trailingDays.size() != d) {
                trailingDays.add(temporarySum);
            } else {
                trailingDays.remove();
                trailingDays.add(temporarySum);
            }

            if (nextDay.getDate() == 0) continue;
            // Parsing the Integer dates
            Period period = Period.between(getDateFromNumber(presentDay.getDate()), getDateFromNumber(nextDay.getDate()));
            skippedDays = period.getDays();

            /* Adding extra days with 0 transactions in queue
               if there are missing days between current and next day */
            for (int j = 0; j < (skippedDays - 1); j++) {
                if (trailingDays.size() != d) {
                    trailingDays.add(0.0);
                } else {
                    trailingDays.remove();
                    trailingDays.add(0.0);
                }
            }

            // Converting queue into ArrayList to sort it using QuickSort
            days = new ArrayList<>(trailingDays);
            if (days.size() == d) quickSort(days, 0, d - 1);
        }

        System.out.println(alerts);
    }
}


/* A class used to store the date and transaction */
class Day {
    private final Integer date;
    private final double transaction;

    Day(Integer date, double transaction) {
        this.date = date;
        this.transaction = transaction;
    }

    double getTransaction() {
        return this.transaction;
    }

    Integer getDate() {
        return this.date;
    }
}
