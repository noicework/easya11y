[#import "/easya11y/templates/includes/class.ftl" as utils]

<input type="hidden" 
       name="${content.name!""}" 
       value="${content.value!""}"
       class="${utils.getClassName(content.class!"")}"
       style="${content.css!""}"/>