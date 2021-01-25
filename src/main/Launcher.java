package main;
import com.google.gson.Gson;
import model.*;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;
import java.util.*;
import static java.lang.Double.parseDouble;
import static spark.Spark.*;

public class Launcher {
    public static void main(String[] args) {
        staticFiles.location("/public");
        String path = "proizvodi.json";
        port(5000);
        ArrayList<Proizvod> korpa = new ArrayList<>();
        HashMap<String, Object> polja = new HashMap<>();
//        ArrayList<Proizvod> listaProizvoda = new ArrayList<>();
//        for (Proizvod p : Data.readFromJson(path)) {
//            if(p.getVisible()) {
//               listaProizvoda.add(p);
//            }
//        }
        // Prikaz proizvoda
        get("/", (request, response) -> {
            ArrayList<Proizvod> proizvodi = new ArrayList<>();
            for (Proizvod p : Data.readFromJson(path)) {
                if(p.getVisible()) {
                    proizvodi.add(p);
                }
            }
            polja.put("proizvodi", proizvodi);
            return new ModelAndView(polja, "index.hbs");
        }, new HandlebarsTemplateEngine());

        // Pretraživanje
        get("/pretraga", (request, response) -> {
            String pretraga = request.queryParams("pretraga").toUpperCase();
            ArrayList<Proizvod> proizvodi = new ArrayList<>();
            for (Proizvod p : Data.readFromJson(path)) {
                if(p.getVisible()) {
                    proizvodi.add(p);
                }
            }
            polja.put("proizvodi", proizvodi.stream().filter(p -> p.getName().toUpperCase().contains(pretraga) || p.getCpu().toUpperCase().contains(pretraga) || p.getMemory().toUpperCase().contains(pretraga) || p.getRam().toUpperCase().contains(pretraga) || p.getScreenResolution().toUpperCase().contains(pretraga) || p.getScreenSize().toUpperCase().contains(pretraga)).toArray());
            return new ModelAndView(polja, "index.hbs");
        }, new HandlebarsTemplateEngine());

        // Login forma učitavanje
        get("/loginForm", (request, response) -> new ModelAndView(null, "login.hbs"), new HandlebarsTemplateEngine());

        // Logovanje
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

        // Učitavanje admin panela
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

        // Dohvatanje proizvoda po kategoriji
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

        // Uništavanje sesije
        get("/logout", (request, response) -> {
            request.session().removeAttribute("user");
            response.redirect("/");
            return null;
        }, new HandlebarsTemplateEngine());

        // Sortiranje proizvoda
        get("/api/sortiraj", (request, response) -> {
//            response.type("text/text");
            String sortiranje = request.queryParams("sort");
            ArrayList<Proizvod> list = new ArrayList<>();
            for (Proizvod p : Data.readFromJson(path)) {
                if(p.getVisible()) {
                    list.add(p);
                }
            }
            for (int i = 0; i < list.size() - 1; i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    switch (sortiranje) {
                        case "1": // Sortiranje po ceni rastuće
                            if (list.get(i).getPrice() > list.get(j).getPrice()) {
                                Proizvod tmp = list.get(i);
                                list.set(i, list.get(j));
                                list.set(j, tmp);
                            }
                             break;
                        case "2": // sortiranje po ceni opadajuće
                            if (list.get(i).getPrice() < list.get(j).getPrice()) {
                                Proizvod tmp = list.get(i);
                                list.set(i, list.get(j));
                                list.set(j, tmp);
                            }
                            break;
                        case "3": // Abecedno A-Z
                            if (list.get(i).getName().compareTo(list.get(j).getName()) > 0) {
                                Proizvod tmp = list.get(i);
                                list.set(i, list.get(j));
                                list.set(j, tmp);
                            }
                            break;
                        case "4": // Abecedno Z-A
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

        // Učitavanje stranice za dodavanje proizvoda
        get("/dodaj", (request, response) -> {
            if (request.session().attribute("user") == null) {
                response.redirect("/");
                return null;
            }
            return new ModelAndView(null,"dodaj.hbs");
        },new HandlebarsTemplateEngine());

        // Dodavanje novog proizvoda
        post("/dodajProizvod", (request, response) -> {
            String naziv = request.queryParams("naziv");
            String cena = request.queryParams("cena");
            String url = request.queryParams("url");
            String[] ekran = request.queryParams("ekran").split(",");
            String[] procesor = request.queryParams("procesor").split(",");
            String[] memorija = request.queryParams("memorija").split(",");
            String poruka = "";
            boolean provera = true;
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

        // Pronalazak proizvoda za izmenu
        get("/izmeni/:id", (request, response) -> {
            if (request.session().attribute("user") == null) {
                response.redirect("/");
                return null;
            }
            int id = Integer.parseInt(request.params("id"));
            for (Proizvod p : Data.readFromJson(path)) {
                if (p.getId() == id) {
                    polja.put("proizvod", p);
                }
            }
            polja.put("poruka", null);
            return new ModelAndView(polja, "izmeni.hbs");
        }, new HandlebarsTemplateEngine());

        // Čuvanje izmena
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

        // Brisanje proizvoda
        get("/obrisi", (request, response) -> {
            response.type("text/text");
            int id = Integer.parseInt(request.queryParams("id"));
            ArrayList<Proizvod> lista = Data.readFromJson(path);
            String poruka = "";
            for (int i = 0; i < lista.size(); i++) {
                if (lista.get(i).getId() == id) {
                    lista.remove(i);
                    poruka = "Uspešno obrisan proizvod";
                    Data.writeToJSON(lista, path);
                    break;
                } else {
                    poruka = "Proizvod nije obrisan";
                }
            }
            return poruka;
        });

        // Učitavanje stranice za dodavanje kategorije
        get("/dodajKategoriju", (request, response) -> {
            if (request.session().attribute("user") == null) {
                response.redirect("/");
                return null;
            }
            return new ModelAndView(null, "dodajKategoriju.hbs");
        },new HandlebarsTemplateEngine());

        // Dodavanje nove kategorije
        post("/dodajKat", (request, response) -> {
            if (request.session().attribute("user") == null) {
                response.redirect("/");
                return null;
            }
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

        // Brisanje kategorije
        get("/izbrisiKategoriju", (request, response) -> {
            String kat = request.queryParams("kategorija");
            String poruka = "";
            ArrayList<Kategorija> kategorije = Data2.readFromJson("kategorije.json");
            boolean provera = false;
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

        // Promena vidljivosti proizvoda
        post("/promeniVidljivost",(request, response) -> {
            response.type("text/text");
            Boolean vidljivost = Boolean.parseBoolean(request.queryParams("visible"));
            int id = Integer.parseInt(request.queryParams("id"));
            ArrayList<Proizvod> proizvodi = Data.readFromJson(path);
            for (Proizvod p : proizvodi) {
                if(p.getId() == id) {
                    p.setVisible(vidljivost);
                    break;
                }
            }
            Data.writeToJSON(proizvodi,path);
            return "Uspesno izmenjena vidjivost";
        });

       // Dodavanje proizvoda u kategoriju
        post("/dodajKorpu",(request, response) -> {
            int id = Integer.parseInt(request.queryParams("id"));
            String poruka = "";
            for (Proizvod p : Data.readFromJson(path)) {
                if(p.getId() == id) {
                    korpa.add(p);
                    poruka = "Proizvod " + p.getName() + " uspesno dodat u korpu";
                    break;
                }
            }
            return poruka;
        });

        // Učitavanje korpe
        get("/korpa", (request, response) -> {
            polja.put("korpa",korpa);
            return new ModelAndView(polja,"korpa.hbs");
        }, new HandlebarsTemplateEngine());

        // Brisanje proizvoda iz korpe
        post("/izbaciProizvod", (request, response) -> {
            int id = Integer.parseInt(request.queryParams("id"));
            for (int i = 0;i< korpa.size();i++) {
                if(korpa.get(i).getId() == id) {
                    korpa.remove(i);
                }
            }
            return "Uspesno izbacen proizvod";
        });


    }
}
