<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=true; section>
    <#if section = "header">
        ${(question)!""}
    <#elseif section = "form">
        <#if (answers)?? && answers?has_content>
            <form id="kc-user-choice-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
                <div class="${properties.kcFormGroupClass!}">
                    <#list answers as answer>
                        <div class="${properties.kcInputWrapperClass!}">
                            <label class="${properties.kcLabelClass!}">
                                <input type="radio" name="answer" value="${answer}" required
                                       <#if answer_index == 0>checked</#if> />
                                ${answer}
                            </label>
                        </div>
                    </#list>
                </div>

                <div class="${properties.kcFormGroupClass!}">
                    <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                               type="submit" value="${msg("userChoiceSubmit")}" />
                    </div>
                </div>
            </form>
        </#if>
    </#if>
</@layout.registrationLayout>
