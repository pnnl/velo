<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <generator version="${server.version}">Alfresco (${server.edition})</generator>
  <title>Thumbnails: ${url.extension}</title> 
  <updated>${xmldate(date)}</updated>
  <icon>${absurl(url.context)}/images/logo/AlfrescoLogo16.ico</icon>
<#list thumbnails as node>
  <entry>
  	<id>${node.parentId}|${node.id}</id>
    <title>${node.name}</title>
    <summary>${node.width}x${node.height}</summary>
  </entry>
</#list>
</feed>
