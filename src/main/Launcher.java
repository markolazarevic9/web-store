package main;
import com.google.gson.Gson;
import model.Data;
import model.Data2;
import model.Kategorija;
import model.Proizvod;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import static java.lang.Double.parseDouble;
import static spark.Spark.*;
import static spark.Spark.staticFiles;

public class Launcher {
    public static void main(String[] args) {
        staticFiles.location("/public");
        String path = "proizvodi.json";
        port(5000);

        HashMap<String, Object> polja = new HashMap<>();
        get("/", (request, response) -> {
            ArrayList<Proizvod> proizvodi = new ArrayList<>();
            for (Proizvod p : Data.readFromJson(path)) {
                if(p.getVisible() == true) {
                    proizvodi.add(p);
                }
            }
            polja.put("proizvodi", proizvodi);
            return new ModelAndView(polja, "index.hbs");
        }, new HandlebarsTemplateEngine());

        get("/pretraga", (request, response) -> {
            String pretraga = request.queryParams("pretraga").toUpperCase();
            ArrayList<Proizvod> proizvodi = Data.readFromJson(path);
            polja.put("proizvodi", proizvodi.stream().filter(p -> p.getName().toUpperCase().contains(pretraga) || p.getCpu().toUpperCase().contains(pretraga)).toArray());
            return new ModelAndView(polja, "index.hbs");
        }, new HandlebarsTemplateEngine());

        get("/loginForm", (request, response) -> {
            return new ModelAndView(null, "login.hbs");
        }, new HandlebarsTemplateEngine());

        post("/login", (request, response) -> {
            String username = request.queryParams("username");
            String password = request.queryParams("password");
            if (username.equals("admin") && password.equals("admin")) {
                request.session().attribute("user", "admin");
                response.redirect("/adminPanel");
                return null;
            }
            return new ModelAndView(null, "login.hbs");
        }, new HandlebarsTemplateEngine());

        get("/adminPanel", (request, response) -> {
            if (request.session().attribute("user") == null) {
                response.redirect("/");
                return null;
            }
            ArrayList<Kategorija> kategorije = Data2.readFromJson("kategorije.json");
            ArrayList<String> nazivKat = new ArrayList<>();
            for (Kategorija k : kategorije) {
                if (!nazivKat.contains(k.getNaziv())) {
                    nazivKat.add(k.getNaziv());
                }

            }
            polja.put("kategorije", nazivKat);
            polja.put("proizvodi", Data.readFromJson(path));
            return new ModelAndView(polja, "adminPanel.hbs");
        }, new HandlebarsTemplateEngine());

        get("/dohvatiProizvode", (request, response) -> {
            response.type("text/text");
            String kategorija = request.queryParams("kategorija");
            ArrayList<Proizvod> lista = new ArrayList<>();
            ArrayList<Proizvod> proizvodi = Data.readFromJson(path);
            for (Proizvod p : proizvodi) {
                String ime = p.getName().toUpperCase();
                if (ime.contains(kategorija)) {
                    lista.add(p);
                }
            }
            if (kategorija.equals("0")) {
                lista = Data.readFromJson(path);
            }
            Gson gson = new Gson();
            return gson.toJson(lista);
        });

        get("/logout", (request, response) -> {
            request.session().removeAttribute("user");
            response.redirect("/");
            return null;
        }, new HandlebarsTemplateEngine());

        get("/api/sortiraj", (request, response) -> {
            response.type("text/text");
            String sortiranje = request.queryParams("sort");
            ArrayList<Proizvod> list = Data.readFromJson(path);
            for (int i = 0; i < list.size() - 1; i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    // Switch -> case
                    switch (sortiranje) {
                        case "1":
                            if (list.get(i).getPrice() > list.get(j).getPrice()) {
                                Proizvod tmp = list.get(i);
                                list.set(i, list.get(j));
                                list.set(j, tmp);
                            }
                             break;
                        case "2":
                            if (list.get(i).getPrice() < list.get(j).getPrice()) {
                                Proizvod tmp = list.get(i);
                                list.set(i, list.get(j));
                                list.set(j, tmp);
                            }
                            break;
                        case "3":
                            if (list.get(i).getName().compareTo(list.get(j).getName()) > 0) {
                                Proizvod tmp = list.get(i);
                                list.set(i, list.get(j));
                                list.set(j, tmp);
                            }
                            break;
                        case "4":
                            if (list.get(i).getName().compareTo(list.get(j).getName()) < 0) {
                                Proizvod tmp = list.get(i);
                                list.set(i, list.get(j));
                                list.set(j, tmp);
                            }
                            break;
                    }

                }
            }
            Gson gson = new Gson();
            return gson.toJson(list);
        });
        post("/dodajProizvod", (request, response) -> {
            String naziv = request.queryParams("naziv");
            String cena = request.queryParams("cena");
            String url = request.queryParams("url");
            String[] ekran = request.queryParams("ekran").split(",");
            String[] procesor = request.queryParams("procesor").split(",");
            String[] memorija = request.queryParams("memorija").split(",");
            String poruka = "";
            Boolean provera = true;
            ArrayList<Proizvod> lista = Data.readFromJson(path);
            Proizvod p = new Proizvod(Data.readFromJson(path).size() + 1, Double.parseDouble(cena), naziv, url, ekran[0], procesor[0], ekran[1], procesor[1], memorija[0], memorija[1]);
            if (lista.size() > 0) {
                for (int i = 0; i < lista.size(); i++) {
                    if (lista.get(i).getName().equals(naziv)) {
                            provera = true;
                            break;
                    } else {
                            if (i == (lista.size() - 1)) {
                                    lista.add(p);
                                    provera = false;
                                    break;
                            }
                    }
                }
            } else {
                lista.add(p);
                provera = false;
            }

            if(!provera){
                poruka = "Uspesno dodat proizvod";
            } else {
                poruka = "Vec postoji proizvod";
            }
            polja.put("poruka", poruka);
            Data.writeToJSON(lista, path);
            return new ModelAndView(polja, "dodaj.hbs");
        }, new HandlebarsTemplateEngine());
//
//        get("/api/obrada", (request, response) -> {
//            response.type("text/text");
//            String filter = request.queryParams("value");
//            ArrayList<Proizvod> proizvodi = Data.readFromJson(path);
//            ArrayList<Proizvod> list = new ArrayList<>();
//            for (Proizvod p : proizvodi) {
//                if (p.getCpu().contains(filter)) {
//                    list.add(p);
//                }
//            }
//            Gson gson = new Gson();
//            return gson.toJson(list);
//        });

        get("/dodaj", (request, response) -> {
            return new ModelAndView(null, "dodaj.hbs");
        }, new HandlebarsTemplateEngine());

        get("/izmeni/:id", (request, response) -> {
            int id = Integer.parseInt(request.params("id"));
            for (Proizvod p : Data.readFromJson(path)) {
                if (p.getId() == id) {
                    polja.put("proizvod", p);
                }
            }
            polja.put("poruka", null);
            return new ModelAndView(polja, "izmeni.hbs");
        }, new HandlebarsTemplateEngine());

        post("/izmeniProizvod", (request, response) -> {
            int id = Integer.parseInt(request.queryParams("id"));
            String naziv = request.queryParams("naziv");
            String cena = request.queryParams("cena");
            String url = request.queryParams("url");
            String[] ekran = request.queryParams("ekran").split(",");
            String[] procesor = request.queryParams("procesor").split(",");
            String[] memorija = request.queryParams("memorija").split(",");

            ArrayList<Proizvod> lista = Data.readFromJson(path);
            for (Proizvod p : lista) {
                if (p.getId() == id) {
                    p.setName(naziv);
                    p.setPrice(parseDouble(cena));
                    p.setImage(url);
                    p.setMemory(memorija[0]);
                    p.setRam(memorija[1]);
                    p.setCpu(procesor[0]);
                    p.setNumberOfCores(procesor[1]);
                    p.setScreenSize(ekran[0]);
                    polja.put("poruka", "Uspesno izmenjeni podaci");
                }
            }

            Data.writeToJSON(lista, path);
            return new ModelAndView(polja, "izmeni.hbs");

        }, new HandlebarsTemplateEngine());

        get("/obrisi", (request, response) -> {
            response.type("text/text");
            int id = Integer.parseInt(request.queryParams("id"));
            ArrayList<Proizvod> lista = Data.readFromJson(path);
            String poruka = "";
            for (int i = 0; i < lista.size(); i++) {
                if (lista.get(i).getId() == id) {
                    lista.remove(i);
                    poruka = "uspesno obrisan proizvod";
                    Data.writeToJSON(lista, path);
                } else {
                    poruka = "proizvod nije obrisan";
                }
            }
            System.out.println(lista.size());

            return poruka;
        });

        get("/dodajKategoriju", (request, response) -> {
            return new ModelAndView(null, "dodajKategoriju.hbs");
        }, new HandlebarsTemplateEngine());

        post("/dodajKat", (request, response) -> {
            String naziv = request.queryParams("naziv").toUpperCase();
            ArrayList<Kategorija> kategorije = Data2.readFromJson("kategorije.json");
            Kategorija k = new Kategorija(kategorije.size() + 1, naziv);
            if (kategorije.size() > 0) {
                for (int i = 0; i < kategorije.size(); i++) {
                    if (kategorije.get(i).getNaziv().equals(naziv)) {
                        break;
                    } else {
                        if (i == kategorije.size() - 1) {
                            kategorije.add(k);
                        }
                    }
                }
            } else {
                kategorije.add(k);
            }
            Data2.writeToJSON(kategorije, "kategorije.json");
            return new ModelAndView(null, "dodajKategoriju.hbs");
        }, new HandlebarsTemplateEngine());

        get("/izbrisiKategoriju", (request, response) -> {
            String kat = request.queryParams("kategorija");
            String poruka = "";
            ArrayList<Kategorija> kategorije = Data2.readFromJson("kategorije.json");
            Boolean provera = false;
            for (Proizvod p : Data.readFromJson(path)) {
                String kategorijaProizvoda = p.getName().split(" ")[0].toUpperCase();
                if (kategorijaProizvoda.equals(kat)) {
                    poruka = "Ne mozete obrisati kategoriju koja ima proizvode";
                    provera = true;
                    break;
                }
            }
            if (!provera) {
                for (Kategorija k : kategorije) {
                    if (kat.equals(k.getNaziv())) {
                        kategorije.remove(k);
                        poruka = "Uspesno obrisana kategorija";
                        Data2.writeToJSON(kategorije, "kategorije.json");
                        break;
                    }
                }
            }
            return poruka;
        });

        post("/promeniVidljivost",(request, response) -> {
            response.type("text/text");
            Boolean vidljivost = Boolean.parseBoolean(request.queryParams("visible"));
            int id = Integer.parseInt(request.queryParams("id"));
            ArrayList<Proizvod> proizvodi = Data.readFromJson(path);
            System.out.println(vidljivost);
            for (Proizvod p : proizvodi) {
                if(p.getId() == id) {
                    p.setVisible(vidljivost);
                    System.out.println(vidljivost);
                    break;
                }
            }
            Data.writeToJSON(proizvodi,path);
            return "Uspesno izmenjena vidjivost";
        });


    }
}
