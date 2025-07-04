[#function getClassName classes='']
  [#assign className = '']

  [#if content.class?has_content]
    [#assign className += content.class]
  [/#if]

  [#if classes?has_content]
    [#if className?has_content]
      [#assign className += ' ${classes}']
    [#else]
      [#assign className += classes]
    [/#if]
  [/#if]

  [#return className]
[/#function]