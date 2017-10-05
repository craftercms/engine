<#import "/templates/web/navigation2/breadcrumb-macros.ftl" as breadcrumbMacros/>

<#macro renderBreadcrumb url root = "/site/website" breadcrumbMacroNS = breadcrumbMacros>
    <#assign breadcrumb = navBreadcrumbBuilder.getBreadcrumb(url, root)>
    <#list breadcrumb as item>
        <@breadcrumbMacroNS.renderBreadcrumbItem item />
    </#list>
</#macro>