import { Box } from '@chakra-ui/react';
import { withErrorBoundary } from 'react-error-boundary';

const _Content = ({ children, fontSize, ...props }) => {
  return <Box
    className="block-content"
    display="grid"
    background="var(--block-surface-color)"
    color="foreground.primary"
    gridTemplateAreas="'main'"
    alignItems="stretch"
    justifyContent="stretch"
    position="relative"
    overflow="visible"
    fontWeight="normal"
    letterSpacing="normal"
    zIndex={2}
    flexGrow={1}
    wordBreak="break-word"
    fontSize={fontSize}
    sx={{
      // DANGER DANGER DANGER
      //
      // The styles below are essential to the
      // basic basic editing workflow.
      // Do not modify them without good reason.
      //
      // make the textarea transparent
      "textarea": {
        display: "block",
        appearance: "none",
        cursor: "text",
        resize: "none",
        transform: "translate3d(0,0,0)",
        color: "inherit",
        outline: "none",
        overflow: "hidden",
        padding: 0,
        background: "var(--block-surface-color)",
        caretColor: "var(--chakra-colors-link)",
        gridArea: "main",
        lineHeight: "inherit",
        minHeight: "100%",
        margin: "0",
        fontSize: "inherit",
        border: 0,
        opacity: 0,
        fontFamily: "inherit"
      },
      "& > span": {
        gridArea: "main",
      },
      // activate interactive content (links, buttons, etc.)
      "a, .url-link, .autolink, link, .hashtag, button, label, video, embed, iframe, img": {
        pointerEvents: "auto",
        zIndex: 2,
      },
      // manage the textarea interactions
      "&:hover textarea:not:(.is-editing)": { lineHeight: 2 },
      "textarea.is-editing": {
        zIndex: 3,
        lineHeight: "inherit",
        opacity: 1,
      },
      "textarea.is-editing ~ *": { opacity: "0" },
      "& > abbr": {
        gridArea: "main",
        zIndex: 4,
        "& > span": {
          position: "relative",
          zIndex: 2,
        }
      },
      // style block children
      "code, pre": {
        fontFamily: "code",
        fontSize: "0.85em",
      },
      ".media-16-9": {
        height: 0,
        width: "calc(100% - 0.25rem)",
        zIndex: 1,
        transformOrigin: "right center",
        transitionDuration: "0.2s",
        transitionTimingFunction: "ease-in-out",
        transitionProperty: "common",
        paddingBottom: "56.25%",
        marginBlock: "0.25rem",
        marginInlineEnd: "0.25rem",
        position: "relative",
      },
      "iframe": {
        border: 0,
        boxShadow: "inset 0 0 0 0.125rem",
        position: "absolute",
        height: "100%",
        width: "100%",
        cursor: "default",
        top: 0,
        right: 0,
        left: 0,
        bottom: 0,
        borderRadius: "0.25rem",
      },
      "img": {
        borderRadius: "0.25rem",
        maxWidth: "calc(100% - 0.25rem)",
      },
      "h1": { fontSize: "xl" },
      "h2": { fontSize: "lg" },
      "h3": { fontSize: "md" },
      "h4": { fontSize: "sm" },
      "h5": { fontSize: "xs" },
      "h6": { fontSize: "xs" },
      "blockquote": {
        marginInline: "0.5em",
        marginBlock: "0.125rem",
        paddingBlock: "calc(0.5em - 0.125rem - 0.125rem)",
        paddingInline: "1.5em",
        borderRadius: "0.25em",
        background: "background.basement",
        borderInlineStart: "1px solid",
        borderColor: "separator.divider",
        color: "foreground.primary",
      },
      "p": {
        paddingBottom: "1em",
        "&last:-child": { paddingBottom: 0 },
      },
    }}
    {...props}
  > {children}</Box>
}

export const Content = withErrorBoundary(_Content, { fallback: <div>oops</div> });