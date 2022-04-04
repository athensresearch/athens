import { Box } from '@chakra-ui/react';

const PAGE_PROPS = {
  as: "article",
  display: "grid",
  flexBasis: "100%",
  gridTemplateAreas: "'header' 'content' 'footer'",
  gridTemplateRows: "auto 1fr auto",
  sx: {
    "--page-padding-v": "6rem",
    "--page-padding-h": "4rem"
  }
}

const TITLE_PROPS = {
  position: "relative",
  gridArea: "title",
  fontSize: "var(--page-title-font-size, 2rem)",
  overflow: "visible",
  flexGrow: "1",
  margin: "0",
  whiteSpace: "pre-line",
  wordBreak: "break-word",
  fontWeight: "bold",
}

export const PageContainer = ({ children, uid, type }) => <Box
  {...PAGE_PROPS}
  maxWidth="70em"
  minHeight="100vh"
  data-ui={uid}
  className={type + '-page'}
  flexDirection="column"
  marginInline="auto"
>{children}</Box>

export const HeaderImage = ({ src }) => <Box
  as="img"
  src={src}
  position="absolute"
  left="0"
  right="0"
  top="0"
  width="100%"
  overflow="hidden"
  opacity="0.125"
  pointerEvents="none"
  objectFit="cover"
  height="20em"
  sx={{
    maskImage: "linear-gradient(to bottom, black, black calc(100% - 4rem), transparent)"
  }}
/>

export const PageHeader = ({ children, image }) => <Box
  as="header"
  pr="var(--page-padding-h)"
  pt="var(--page-padding-v)"
  pb={4}
  gridArea="header"
  display="grid"
  gridTemplateColumns="max(var(--page-padding-h), 3rem) 1fr auto"
  gridTemplateRows="auto auto"
  alignItems="center"
  gridTemplateAreas="'empty breadcrumb breadcrumb' 
  'menu title extras'"
  className="page-header"
  borderBottom="1px solid transparent"

  // Page headers without images get a nice border...
  {...!image && ({
    borderBottomColor: "separator.divider"
  })}
  // unless they're in the sidebar
  sx={{
    ".right-sidebar &": {
      // borderBottomColor: "transparent"
    }
  }}
>
  {image && <HeaderImage src={image} />}
  {children}
</Box>

export const PageBody = ({ children }) => <Box
  as="main"
  pt={4}
  // inset the left margin to account for block toggles
  px="calc(var(--page-padding-h) - 1em)"
  gridArea="content"
>{children}</Box>

export const PageFooter = ({ children }) => <Box
  as="footer"
  pt={4}
  gridArea="footer"
  p={4}
>{children}</Box>

export const TitleContainer = ({ children }) => <Box
  {...TITLE_PROPS}
>{children}</Box>

export const DailyNotesPage = ({ isReal, children }) => <Box
  {...PAGE_PROPS}
  className="node-page daily-notes"
  boxShadow="page"
  bg="background.floor"
  alignSelf="stretch"
  justifySelf="stretch"
  opacity={isReal ? 1 : 0.5}
  borderWidth="1px"
  borderStyle="solid"
  borderColor="separator.divider"
  transitionDuration="0s"
  borderRadius="0.5rem"
  minHeight="calc(100vh - 10rem)"
  sx={{
    "--page-padding-v": "1rem",
    "--page-padding-h": "4rem"
  }}
>{children}</Box>

export const EditableTitleContainer = ({ children, isEditing, props }) => <Box
  {...TITLE_PROPS}
  as="h1"
  className={[
    'page-title',
    isEditing && 'is-editing',
  ].filter(Boolean).join(' ')}
  gridArea="title"
  maxWidth="max-content"
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
      caretColor: "var(--chakra-colors-link)",
      fontSize: "inherit",
      fontWeight: "inherit",
      borderRadius: "0.25rem",
      border: "0",
      opacity: "0",
      fontFamily: "inherit"
    },
    "&:hover: textarea:not:(.is-editing)": { lineHeight: "2" },
    "textarea.is-editing + *": {
      opacity: "0",
    },
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
      pointerEvents: "none",
      "a, button": {
        position: "relative",
        zIndex: 2,
        pointerEvents: "all",
      },
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
  }}
  {...props}>
  {children}
</Box>;