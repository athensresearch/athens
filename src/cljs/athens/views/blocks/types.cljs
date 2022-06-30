(ns athens.views.blocks.types
  "Athens Block/Entity Types")


(defprotocol BlockTypeProtocol
  "Block/Entity Type Protocol for rendering aspects"

  (inline-ref-view
    "Render Block/Entity Type as inline reference"
    [block-data callbacks with-breadcrumb?])

  (outline-view
    "Render Block/Entity Type as outline representation"
    [block-data callbacks])

  (supported-transclusion-scopes
    "Returns a set of supported `transclusion-scopes`"
    [])

  (transclusion-view
    "Render Block/Entity Type as transclusion"
    [block-data callback transclusion-scope])

  (zoomed-in-view
    "Render Block/Entity Type as zoomed in"
    [block-data callbacks])

  (supported-breadcrumb-styles
    "Returns a set of supported `breadcrumb-styles`"
    [])

  (breadcrumbs-view
    "Render Block/Entity Type as breadcrumbs"
    [block-data callbacks breadcrumb-style]))
