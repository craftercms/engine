<#macro renderBreadcrumbItem item >
    <#if item.active>
        <li class="active">${item.label}</li>
    <#else>
        <li><a href="${item.url}">${item.label}</a></li>
    </#if>
</#macro>