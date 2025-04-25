import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class EvalAnalyser {
    public static void main(String[] argc){
        Scanner fileNameScanner = new Scanner(System.in);
        System.out.print("Введите полный путь к папкам: ");

        String folderName = fileNameScanner.nextLine();
        if(folderName.length() == 0){
            System.out.print("Вы не ввели путь к папке.");
            System.exit(1);
        }

        File reportFile = new File(folderName + "\\Отчет.txt");
        try {
            reportFile.createNewFile();
            FileWriter writer = new FileWriter(reportFile);
            writer.write("");
            writer.close();
        }catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        File folder = new File(folderName);

        File[] files = folder.listFiles();

        //Файлы студентов
        Vector<File> students = new Vector<File>(3, 2);

        if(files == null){
            System.out.print("Файлы не найдены.");
            System.exit(1);
        }

        //Имена студентов и их среднии оценки
        List<Map.Entry<Float,String>> students_eval = new ArrayList<>();

        //Предметы
        Map<String,Subject> subjectsName = new HashMap<>();

        for(File file:files){
            if(file.isFile()){ //Проверка на файл
                String fileName = file.getName();
                if(!fileName.trim().isEmpty()){ // Имя файла не пустое
                    String[] words = fileName.trim().split("\\s+");
                    if(words.length == 3){ // Состоит из трех слов
                        students.add(file);
                    }
                }
            }
        }

        for (int i = 0; i < students.size(); i++) {
            //Запись имени студента
            String name = students.get(i).getName();

            //Чтение строк из файла
            List<String> lines = null;
            try {
                lines = Files.readAllLines(Paths.get(students.get(i).getPath()));
            }catch (IOException e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
            Vector<Integer> evals = new Vector<>();

            for(String str: lines){
                evals.addElement(findEval(str));
            }

            float sumEval = 0; // Сумма всех оценок
            for(int eval: evals){
                sumEval += eval;
            }
            float averageEval = sumEval / evals.size(); // средняя оценка ученика

            students_eval.add(Map.entry(averageEval,name));
            for(int j = 0;j<lines.size();j++){
                String subobjectName = lines.get(j).substring(0,lines.get(j).indexOf(' '));
                int eval = findEval(lines.get(j));

                Subject fountObj = subjectsName.get(subobjectName);
                if(fountObj!=null){
                    fountObj.amount += 1;
                    if(eval <= 5 && eval >= 1){
                        fountObj.sumEval += eval;
                    }else{
                        System.out.println("В предмете " + subobjectName + " у ученика " + name.substring(0,name.length()-4) + " ошибочная оценка " + eval + " (она не учитывается)");
                    }
                }else{
                    Subject obj = new Subject(subobjectName,eval);
                    subjectsName.put(subobjectName,obj);
                }
            }
        }
        //Запись в файл предметов
        for(Subject ex : subjectsName.values()) {
            writeMsg(ex.name + " - " + String.format("%.2f",(float) ex.sumEval/ex.amount),reportFile);
        }

        //Запись в файл учеников
        if(students_eval.size() == 1){
            System.out.println("Ученик всего один:");
            writeMsg(students_eval.get(0).getValue().substring(0,students_eval.get(0).getValue().length()-4) + " - "+ String.format("%.2f",students_eval.get(0).getKey()),reportFile);
            return;
        }else{
            students_eval.sort((ex1, ex2) -> Float.compare(ex2.getKey(), ex1.getKey()));
            //Лучший(е) ученики
            if(students_eval.get(0).getKey() == students_eval.get(1).getKey()){
                writeMsg("\nЛучшие ученики:",reportFile);
                for(int i = 0;;i+=2){
                    if(i+1 >= students_eval.size()){
                        return;
                    }
                    if(students_eval.get(i).getKey() == students_eval.get(i+1).getKey()){
                        writeMsg(students_eval.get(i).getValue().substring(i,students_eval.get(i).getValue().length()-4) + " - " + String.format("%.2f",students_eval.get(i).getKey()),reportFile);
                        writeMsg(students_eval.get(i+1).getValue().substring(i+1,students_eval.get(i+1).getValue().length()-4) + " - " + String.format("%.2f", students_eval.get(i+1).getKey()),reportFile);
                    }else{
                        break;
                    }
                }
            }else{
                writeMsg("\nЛучший ученик:",reportFile);
                writeMsg(students_eval.get(0).getValue().substring(0,students_eval.get(0).getValue().length()-4) + " - " +  String.format("%.2f", students_eval.get(0).getKey()),reportFile);
            }
            //Худший(е) ученики

            if(students_eval.get(students_eval.size()-1).getKey() == students_eval.get(students_eval.size()-2).getKey()){
                writeMsg("\nХудшие ученики:",reportFile);
                for(int i = students_eval.size() - 1;;i-=2){
                    if(students_eval.get(i).getKey() == students_eval.get(i-1).getKey()){
                        writeMsg(students_eval.get(i).getValue().substring(i,students_eval.get(i).getValue().length()-4) + " - " + String.format("%.2f",students_eval.get(i).getKey()),reportFile);
                        writeMsg(students_eval.get(i-1).getValue().substring(i-1,students_eval.get(i-1).getValue().length()-4) + " - " + String.format("%.2f", students_eval.get(i-1).getKey()),reportFile);
                    }else {
                        break;
                    }
                }
            }else{
                writeMsg("\nХудший ученик:",reportFile);
                int lastIdx = students_eval.size()-1;
                writeMsg(students_eval.get(lastIdx).getValue().substring(0,students_eval.get(lastIdx).getValue().length()-4) + " - " +  String.format("%.2f", students_eval.get(lastIdx).getKey()),reportFile);
            }

        }
    }
    public static int findEval(String str) {
        try {
            String[] parts = str.split(" - ");
            if (parts.length == 2) {
                return Integer.parseInt(parts[1]);
            } else {
                return -1; // число не найдено
            }
        } catch (NumberFormatException e) {
            return -1; // не число
        }
    }
    public static void writeMsg(String msg, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(msg);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл отчета: " + e.getMessage());
        }
    }
    public static class Subject{ //Предметы
        public Subject(String name, int eval) {
            this.name = name;
            this.sumEval = eval;
            this.amount = 1;
        }
        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
        @Override
        public boolean equals(Object o) { //Сравнение классов по name для hashSet
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Subject subject = (Subject) o;
            return Objects.equals(name, subject.name);
        }

        public String name; // Название предмета
        public float amount;  // Количество встречаемых предметов
        public int sumEval; // сумма всех оценок
    }
}
