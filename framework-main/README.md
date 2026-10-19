# Sprint Mini MVC

Mini framework MVC Java 17, basé sur Java Servlet 4 (`javax.servlet`) et les annotations du projet initial. Il n'utilise pas Spring. Le dépôt est conservé dans son organisation existante : le framework `mg.itu` et l'application de démonstration `com.framework.corps` sont empaquetés ensemble pour faciliter l'essai sur Tomcat 9.0 ou plus récent.

## Construire et déployer

Depuis `framework-main` :

```sh
mvn package
```

Déployer `target/framework.war` dans Tomcat 9.0+. Ouvrir `/framework/` (ou le contexte attribué par Tomcat) pour accéder à la démonstration.

Le fichier `web.xml` configure le package des contrôleurs, le préfixe `/WEB-INF/views/`, le suffixe `.jsp`, le listener d'initialisation, le front controller et les limites d'upload (10 Mo par fichier, 20 Mo par requête).

## Ajouter un contrôleur

```java
@Controller
public class ExampleController {
    @UrlMapping(value = "/example", method = "GET")
    public ModelAndView home(String name) {
        return new ModelAndView("home", Map.of("name", name));
    }

    @UrlMapping(value = "/api/example", method = "GET")
    @Json
    public Map<String, Object> api() {
        return Map.of("ok", true);
    }
}
```

Les routes supportent GET, POST, PUT et DELETE. Au démarrage, le scanner explore le package configuré et ses sous-paquets dans les classes compilées ou les JAR. Les doublons arrêtent l'initialisation avec les deux méthodes en conflit. Les arguments d'une méthode peuvent être des paramètres de formulaire/requête (noms Java conservés par Maven), `HttpServletRequest`, `HttpServletResponse`, `Part` ou `MapFile`.

`ModelAndView` transmet les attributs à une JSP. `@Json` sérialise les chaînes directement et prend en charge les objets, tableaux, collections et maps. Les routes manquantes renvoient HTTP 404 avec la liste des routes connues.

## Upload

Les contrôleurs peuvent recevoir un champ `<input type="file" name="file">` en paramètre `MapFile file`. `MapFile` contient le nom, l'extension et les octets, et peut enregistrer le fichier avec `saveTo(Path)`. L'application de démonstration stocke les fichiers dans le dossier `uploads` de son déploiement. Les noms sont réduits au nom de fichier pour éviter les chemins fournis par le navigateur.

## JDBC

`DbConnection.open()` ouvre une connexion JDBC avec les propriétés système `DB_URL`, `DB_USER` et `DB_PASSWORD`. `GenericDAO<T>` fournit les primitives protégées de sélection, insertion, mise à jour et suppression. Les DAO concrets définissent leurs requêtes préparées et un `RowMapper<T>`. Le pilote JDBC du moteur utilisé doit être ajouté par l'application qui déploie le framework.

## Exemple inclus

- `GET /test` : page d'accueil et formulaire.
- `POST /test/form` : exemple de formulaire et liaison automatique du paramètre.
- `GET /test/json` : endpoint JSON.
- `POST /test/upload` : upload multipart.
- `GET /test/view` : exemple direct de `ModelAndView`.

Les annotations gardent les packages déjà employés dans le projet : `mg.itu.annotation.Controller.Controller`, `mg.itu.annotation.Url.UrlMapping` et `mg.itu.annotation.Json.Json`.
