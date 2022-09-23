(ns athens.types.core
  "Athens Block/Entity Types")


(defprotocol BlockTypeProtocol
  "Block/Entity Type Protocol for rendering aspects"

  (text-view
    [this block-data attr]
    "Renders Block/Entity Type as textual representation.
     Recursively resolves references and all.")

  (inline-ref-view
    [this block-data attr ref-uid uid callbacks with-breadcrumb?]
    "Render Block/Entity Type as inline reference")

  (outline-view
    [this block-data callbacks]
    "Render Block/Entity Type as outline representation")

  (supported-transclusion-scopes
    [this]
    "Returns a set of supported `transclusion-scopes`")

  (transclusion-view
    [this block-el block-uid callback transclusion-scope]
    "Render Block/Entity Type as transclusion")

  (zoomed-in-view
    [this block-data callbacks]
    "Render Block/Entity Type as zoomed in")

  (supported-breadcrumb-styles
    [this]
    "Returns a set of supported `breadcrumb-styles`")

  (breadcrumbs-view
    [this block-data callbacks breadcrumb-style]
    "Render Block/Entity Type as breadcrumbs"))
