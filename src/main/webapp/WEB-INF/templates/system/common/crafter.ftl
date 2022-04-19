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

<#macro body_bottom initializeExperienceBuilder=true xbProps="{}">
    <#include "/templates/system/plugins/body_bottom.ftl" ignore_missing=true />
    <#if initializeExperienceBuilder>
        <@initExperienceBuilder props=xbProps />
    </#if>
</#macro>

<#-- Performs an include also making available the plugin variables in that scope -->
<#macro plugin_include pluginId pluginPath>
    <#assign pluginConfig = pluginService.getPluginConfig(pluginId)/>
    <#include pluginPath ignore_missing=true/>
</#macro>
