(ns athens.views.blocks.types
  "Athens Block/Entity Types")


(defprotocol BlockTypeProtocol
  "Block/Entity Type Protocol for rendering aspects"

  (inline-ref-view
    [this block-data callbacks with-breadcrumb?]
    "Render Block/Entity Type as inline reference")

  (outline-view
    [this block-data callbacks]
    "Render Block/Entity Type as outline representation")

  (supported-transclusion-scopes
    [this]
    "Returns a set of supported `transclusion-scopes`")

  (transclusion-view
    [this block-data callback transclusion-scope]
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


(defmulti block-type->protocol
  "Takes `:block/type` value and converts it to `BlockTypeProtocol` to be used for rendering"
  (fn [block-type]
    block-type))


(defmethod block-type->protocol :default [block-type]
  (throw (ex-info "Illegal argument, `block-type` not specified." {:block/type block-type})))
