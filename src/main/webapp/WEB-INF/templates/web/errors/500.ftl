<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
    <head>
        <title>Internal Server Error</title>

        <style>
            body {
                font-family: sans-serif; color: #222;
            }
            .stackTrace {
                border: 1px solid;
                padding: 10px;
            }
        </style>
    </head>
    <body>
        <h1>Oops! A server error has occurred and we were unable to fulfill the request.<br/>
        Please try again. If the error persists, contact the administrator of the site.</h1>
        <#if modePreview??><pre class="stackTrace">${stackTrace}</pre></#if>
    </body>
</html>