{
    <#list values?keys as key>
    "${key}": "${values[key]}"<#if key_has_next>,</#if>
    </#list>
}