<#include "/templates/system/common/ice.ftl"/>
<#include "/templates/system/common/navigation.ftl"/>
<#include "/templates/system/common/breadcrumb.ftl"/>
<#include "/templates/system/plugins/definitions.ftl" ignore_missing=true />

<#macro head>
    <#include "/templates/system/plugins/head.ftl" ignore_missing=true />
</#macro>

<#macro body_top>
    <#include "/templates/system/plugins/body_top.ftl" ignore_missing=true />
</#macro>

<#macro body_bottom initializeInContextEditing=true ICEProps="{}">
    <#include "/templates/system/plugins/body_bottom.ftl" ignore_missing=true />
    <#if initializeInContextEditing>
        <@initInContextEditing props=ICEProps />
    </#if>
</#macro>