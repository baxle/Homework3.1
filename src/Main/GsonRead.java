package Main;

import Company.MyPojo;
import Company.Security;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


public class GsonRead {
    private static final String FILENAME = "test.json";
    private static List<MyPojo> companies;
    private static List<MyPojo> temps;
    private static List<Security> securities;



    public static void main(String[] args)  {
        if (!Files.exists(Paths.get(FILENAME))) {
            System.err.println("Файла не существует. Проверьте адрес файла.");
            System.exit(0);
        }
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(FILENAME));
                companies = new Gson().fromJson(bufferedReader, new TypeToken<List<MyPojo>>() {
                }.getType());
                System.out.println("Выводим на экран все имеющиеся компании:");
                System.out.println(companies);
                System.out.println("\n" + "Выводим на экран все ценные бумаги, просроченные на текущий момент:");

                long overdueSecuritiesCounter = 0;
                LocalDate today = LocalDate.now(ZoneId.of("Europe/Moscow"));

                for (MyPojo comp : companies) {
                    for (Security security : comp.getSecurities()) {
                        if (LocalDate.parse(security.getDate_to(), DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)).isBefore(today)) {
                            System.out.println("Class Security: Код = " + security.getCode() + ", полное имя владельца = " + comp.getname_full() + ", дата истечения = " + security.getDate_to());
                            overdueSecuritiesCounter++;
                        }
                    }
                }

                System.out.println("\n" + "Суммарное число просроченных бумаг " + overdueSecuritiesCounter);
            } catch (IOException e) {
                e.printStackTrace();
            }


//Запросы пользователя


        System.out.println("\n" + "Введите дату в формате «ДД.ММ.ГГ», «ДД.ММ.ГГГГ» (без кавычек) для получения списка организаций, основанных после введенной даты.");
        System.out.println("Введите код валюты в формате «EU», «USD», «RUB» (без кавычек) для получения информации о ценных бумагах. Для завершения программы введите «exit».");
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(inputStreamReader);
        String line;
        try {
            while (!(line = br.readLine()).equals("exit")) {
                if (line.length()==2||line.length()==3) {
                    final String a = line;
                    securities = companies.stream()
                            .flatMap(c -> Arrays.stream(c.getSecurities())).filter(x -> x.getCurrency().getCode().equals(a))
                            .collect(Collectors.toList());

                    if (securities.isEmpty()) {
                        System.out.println("Не существует введенной валюты. Для выхода из программы введите exit.");
                    } else {
                        securities.forEach(System.out::println);
                    }

                } else if (isValidDate(line)) {
                    String dateWithoutDelimiter = line.replace(".", "").replace("/", "");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateWithoutDelimiter.length() == 6 ? "ddMMyy" : "ddMMyyyy");
                    LocalDate inputDate = LocalDate.parse(dateWithoutDelimiter, formatter);
                    temps = companies.stream()
                            .filter(x -> LocalDate.parse(x.getEgrul_date(), DateTimeFormatter.ofPattern("yyyy-MM-dd")).isAfter(inputDate))
                            .collect(Collectors.toList());

                    if (temps.isEmpty()) {
                        System.out.println("Не существует организаций с датой основания позже введенной. Для выхода из программы введите exit.");
                    } else {
                        temps.forEach(System.out::println);
                    }
                } else {
                    System.err.println("Проверьте правильность введенных данных.");
                    System.err.println("Возможные варианты: EU, USD, RUB, дата в формате «ДД.ММ.ГГ», «ДД.ММ.ГГГГ», «ДД/ММ/ГГ», «ДД/ММ/ГГГГ»");
                    System.err.println("Для выхода из программы введите exit.");
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private static boolean isValidDate(String line) {
        if (line.length() != 8 && line.length() != 10) return false;

        line = line.replace(".", "").replace("/", "");
        return (line.length() == 6 || line.length() == 8);
    }
}

