/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wordreferencetocsv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Martin
 */
public class WordReferenceToCSV {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String filePath = args[0];
        String nombreArchivoSalida = "src/set_verbos_conj.txt";
        BufferedReader fileReader = null;
        BufferedWriter bufferEscritura = null;
        try {
            bufferEscritura = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nombreArchivoSalida), "utf-8"));
            bufferEscritura.write("infinitivo,person,number,tense,mood,conjugacion\n");
            FileInputStream is = new FileInputStream(filePath);
            //InputStreamReader isr = new InputStreamReader(is, "Windows-1252");
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            fileReader = new BufferedReader(isr);
            String currentLineString = fileReader.readLine();
            while (currentLineString != null) {
                Document doc = (Document) Jsoup.connect("http://www.wordreference.com/conj/EsVerbs.aspx")
                        .data("v", currentLineString).get();
                docToCSV(doc, bufferEscritura, currentLineString);
                currentLineString = fileReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null) {
                    // close reader, good practice
                    fileReader.close();
                }
                if (bufferEscritura != null) {
                    bufferEscritura.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Recorrer el document y crear el csv
     *
     * @param doc
     * @param buff
     */
    private static void docToCSV(Document doc, BufferedWriter buff, String verbo) throws IOException {
        Elements elements = doc.getElementsByClass("aa");
        int mood;
        if (!elements.text().equals("")) {
            for (Element element : elements) {
                switch (element.getElementsByTag("h4").text()) {
                    case "Indicativo":
                        mood = 0;
                        doInst(element, buff, verbo, mood);
                        break;
                    case "Subjuntivo":
                        mood = 1;
                        doInst(element, buff, verbo, mood);
                        break;
                    case "Imperativo":
                        mood = 2;
                        doInst(element, buff, verbo, mood);
                        break;
                }
            }
        } else {
            System.out.println(verbo + " no encontrado");
        }
    }

    private static void doInst(Element elem, BufferedWriter buff, String verbo, int mood) throws IOException {
        int tense;
        int person;
        int number;
        String conj = "";
        for (Element e : elem.getElementsByClass("neoConj")) {
            Element tbody = e.getElementsByTag("tbody").get(0);
            tense = getTense(tbody.child(0).text());
            for (int j = 1; j < tbody.childNodeSize(); j++) {
                person = getPerson(tbody.child(j).getElementsByTag("th").text());
                number = getNumber(tbody.child(j).getElementsByTag("th").text());
                conj = tbody.child(j).getElementsByTag("td").text().split(" ")[0]; //se queda con la primer conjugacion en caso que hayan 2 posibles
                String inst = verbo + "," + person + "," + number + "," + tense + "," + mood + "," + conj;
                if (!tbody.child(j).getElementsByTag("th").text().equals("vos")
                        && !tbody.child(j).getElementsByTag("th").text().equals("(vos)")
                        && !tbody.child(0).text().equals("pretérito anterior")
                        && !tbody.child(0).text().equals("negativo")) {
                    //System.out.println(inst);
                    buff.write(inst + "\n");
                }
            }
        }
    }

    private static int getTense(String t) {
        int tense = 0;
        if (t.contains("presente")) {
            tense = 0;
        } else if (t.contains("imperfecto")) {
            tense = 1;
        } else if (t.contains("pretérito")) {
            tense = 2;
        } else if (t.contains("futuro")) {
            tense = 3;
        } else if (t.contains("condicional")) {
            tense = 4;
        }
        return tense;
    }

    private static int getPerson(String p) {
        int person = 0;
        if (p.contains("yo") || p.contains("nosotros")) {
            person = 1;
        } else if (p.contains("tú") || p.contains("vosotros")) {
            person = 2;
        } else if (p.contains("él") || p.contains("ellos")) {
            person = 3;
        }
        return person;
    }

    private static int getNumber(String n) {
        int num = 0;
        if (n.contains("yo") || n.contains("tú") || n.contains("él")) {
            num = 0;
        } else if (n.contains("nosotros") || n.contains("vosotros") || n.contains("ellos")) {
            num = 1;
        }
        return num;
    }
}
