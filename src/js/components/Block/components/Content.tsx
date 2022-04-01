import { Box } from '@chakra-ui/react';

export const Content = ({ children, fontSize }) => {
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
    zIndex={2}
    flexGrow={1}
    wordBreak="break-word"
    fontSize={fontSize}
    sx={{
      "textarea": {
        display: "block",
        lineHeight: 0,
        appearance: "none",
        cursor: "text",
        resize: "none",
        transform: "translate3d(0,0,0)",
        color: "inherit",
        outline: "none",
        overflow: "hidden",
        padding: "0",
        background: "var(--block-surface-color)",
        gridArea: "main",
        minHeight: "100%",
        margin: "0",
        fontSize: "inherit",
        borderRadius: "0.25rem",
        border: "0",
        opacity: "0",
        fontFamily: "inherit"
      },
      "&:hover: textarea:not:(.is-editing)": { lineHeight: "2" },
      "textarea.is-editing + *": { opacity: "0" },
      ".is-editing": {
        zIndex: 3,
        lineHeight: "inherit",
        opacity: 1,
      },
      "span.text-run": {
        pointerEvents: "none",
        "& > a": {
          position: "relative",
          zIndex: 2,
          pointerEvents: "all",
        }
      },
      "span": {
        gridArea: "main",
        "& > span": {
          position: "relative",
          zIndex: 2,
        }
      },
      "abbr": {
        gridArea: "main",
        zIndex: 4,
        "& > span": {
          position: "relative",
          zIndex: 2,
        }
      },
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
      "mark.contents.highlight": {
        padding: "0 0.2em",
        borderRadius: "0.125rem",
        background: "highlight",
      }
    }
    }
  > {children}</Box>
}