<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:addResources type="css" resources="jquery-ui.min.css, jquery-ui.structure.min.css, jquery-ui.theme.min.css, autoCompleteSearch.css" />
<template:addResources type="javascript" resources="jquery-ui.min.js" /> 

<form>
  <input type="text" id="search" style="width:100%;"/>
</form>


<script>

  $( function() {
      $.widget( "custom.catcomplete", $.ui.autocomplete, {
          _create: function() {
              this._super();
              this.widget().menu( "option", "items", "> :not(.ui-autocomplete-category)" );
          },
          _renderMenu: function( ul, items ) {
              var that = this,
                  currentCategory = "";
              var categoryArray = [];
              $.each( items, function( index, item ) {
                  categoryArray[item.category] = true;
              });
              for (var key in categoryArray) {
                  ul.append( "<li class='ui-autocomplete-category'>" + key + "</li>" );

                  $.each( items, function( index, item ) {
                      if (key == item.category) {
                        var li;
                        li = that._renderItemData( ul, item );
                        if ( item.category ) {
                           li.attr( "aria-label", item.category + " : " + item.value );
                        }
                      }
                  });
              }

          }
      });


      $( "#search" ).catcomplete({
          source: "${url.base}${currentNode.path}.search.html.ajax",
          delay: 50,
          select: function( event, ui ) {
              window.location.href = ui.item.url;
              return false;
          }
      });
  } );
  </script>