<#import "/templates/web/navigation2/nav-macros.ftl" as navMacros/>

<#macro renderNavigation url depth includeRoot = false navMacrosNs = navMacros>
    <#assign navTree = navTreeBuilder.getNavTree(url, depth, Request.pageUrl)/>

    <#if includeRoot>
        <@navMacrosNs.renderRootItem navTree/>
    </#if>

    <#assign subItems = navTree.subItems/>
    <#if (subItems?size > 0)>
        <@renderNavigationItems subItems 1 navMacros/>
    </#if>
</#macro>

<#macro renderNavigationItems navItems currDepth navMacrosNs>
    <#list navItems as navItem>
        <#assign subItems = navItem.subItems/>
        <#if currDepth == 1>
            <#if (subItems?size > 0)>
                <@navMacrosNs.renderNavItemWithSubItems navItem>
                    <@renderNavigationItems subItems, currDepth + 1, navMacros/>
                </@navMacrosNs.renderNavItemWithSubItems>
            <#else>
                <@navMacrosNs.renderNavItem navItem/>
            </#if>
        <#else>
            <#if (subItems?size > 0)>
                <@navMacrosNs.renderNavSubItemWithSubItems navItem>
                    <@renderNavigationItems subItems, currDepth + 1, navMacros/>
                </@navMacrosNs.renderNavSubItemWithSubItems>
            <#else>
                <@navMacrosNs.renderNavSubItem navItem/>
            </#if>
        </#if>
    </#list>
</#macro>