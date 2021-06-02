<#include "/templates/system/common/ice.ftl"/>
<#include "/templates/system/plugins/definitions.ftl" ignore_missing=true />

<#macro head>
    <#include "/templates/system/plugins/head.ftl" ignore_missing=true />
</#macro>

<#macro body_top>
    <#include "/templates/system/plugins/body_top.ftl" ignore_missing=true />
</#macro>

<#macro body_bottom initializePageBuilder=true pageBuilderProps="{}">
    <#include "/templates/system/plugins/body_bottom.ftl" ignore_missing=true />
    <#if initializePageBuilder>
        <@initPageBuilder props=pageBuilderProps />
    </#if>
</#macro>
