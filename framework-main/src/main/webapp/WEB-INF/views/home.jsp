<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<% String rawName = String.valueOf(request.getAttribute("name"));
   String safeName = rawName.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); %>
<!doctype html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title}</title>
    <style>
        body { max-width: 760px; margin: 3rem auto; padding: 0 1rem; font: 16px/1.5 system-ui, sans-serif; color: #172033; }
        section { margin: 1.5rem 0; padding: 1rem 1.25rem; border: 1px solid #d9dfeb; border-radius: 12px; }
        input, button { margin: .35rem 0; padding: .55rem; }
        a { color: #1459bd; }
    </style>
</head>
<body>
    <h1>${title}</h1>
    <p>Exemple web qui utilise le mini framework MVC du projet Sprint.</p>
    <% if (request.getAttribute("name") != null) { %>
        <p>Résultat : <strong><%= safeName %></strong></p>
    <% } %>
    <section>
        <h2>Formulaire POST</h2>
        <form method="post" action="${pageContext.request.contextPath}/test/form">
            <label>Votre nom <input name="name" required></label>
            <button type="submit">Envoyer</button>
        </form>
    </section>
    <section>
        <h2>Upload multipart</h2>
        <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/test/upload">
            <label>Fichier <input type="file" name="file" required></label>
            <button type="submit">Télécharger</button>
        </form>
    </section>
    <section>
        <h2>API JSON</h2>
        <p><a href="${pageContext.request.contextPath}/test/json">GET /test/json</a></p>
    </section>
</body>
</html>
