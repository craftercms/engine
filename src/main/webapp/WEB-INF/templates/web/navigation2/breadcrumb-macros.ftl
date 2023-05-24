<#macro renderBreadcrumbItem item >
    <#if item.active>
        <li class="active"><#outputFormat "HTML">${item.label}</#outputFormat></li>
    <#else>
        <li><a href="${item.url}"><#outputFormat "HTML">${item.label}</#outputFormat></a></li>
    </#if>
</#macro>
