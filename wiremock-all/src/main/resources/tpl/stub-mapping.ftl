<html>
<#-- @ftlvariable name="data" type="com.dtuchs.libs.wiremock.base.allure.StubMappingAttachment" -->
<head>
    <meta http-equiv="content-type" content="text/html; charset = UTF-8">
    <script src="https://yastatic.net/jquery/2.2.3/jquery.min.js" crossorigin="anonymous"></script>

    <link href="https://yastatic.net/bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet" crossorigin="anonymous">
    <script src="https://yastatic.net/bootstrap/3.3.6/js/bootstrap.min.js" crossorigin="anonymous"></script>

    <link type="text/css" href="https://yandex.st/highlightjs/8.0/styles/github.min.css" rel="stylesheet"/>
    <script type="text/javascript" src="https://yandex.st/highlightjs/8.0/highlight.min.js"></script>
    <script type="text/javascript" src="https://yandex.st/highlightjs/8.0/languages/json.min.js"></script>
    <script type="text/javascript">hljs.initHighlightingOnLoad();</script>

    <style>
        pre {
            white-space: pre-wrap;
        }
    </style>
</head>
<body>
<div>
    <pre><code><#if data.method??>${data.method}<#else>GET</#if>: <#if data.stubUrl??>${data.stubUrl}<#else>Unknown</#if></code></pre>
</div>
<#if (data.stubBodyPatterns)?has_content>
    <h4>Stub body patterns</h4>
    <#list data.stubBodyPatterns as pattern>
        <div>
            <div>
                <pre><code><b>${pattern}</b></code></pre>
            </div>
        </div>
    </#list>
</#if>

<#if data.responseBody??>
    <h4>Should return Body</h4>
    <div>
        <pre><code>${data.responseBody}</code></pre>
    </div>
</#if>

<#if data.responseStatus??>
    <h4>Should return code</h4>
    <div>
        <pre><code>${data.responseStatus}</code></pre>
    </div>
</#if>
</body>
</html>