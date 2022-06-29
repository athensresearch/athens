(ns athens.views.blocks.types
  "Athens Block/Entity Types")


(defprotocol BlockTypeProtocol
  "Block/Entity Type Protocol for rendering aspects"

  (inline-ref-view
    "Render Block/Entity Type as inline reference"
    [block-data callbacks])

  (outline-view
    "Render Block/Entity Type as outline representation"
    [block-data callbacks])

  (transclusion-view
    "Render Block/Entity Type as transclusion"
    [block-data callback transclusion-scope])

  (zoomed-in-view
    "Render Block/Entity Type as zoomed in"
    [block-data callbacks])

  (breadcrumbs-view
    "Render Block/Entity Type as breadcrumbs"
    [block-data callbacks breadcrumb-style]))
