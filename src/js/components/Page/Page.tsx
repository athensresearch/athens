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
  fontSize: "2rem",
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
  pb="calc(var(--page-padding-v) / 2)"
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
  gridTemplateColumns="var(--page-padding-h) 1fr auto"
  gridTemplateRows="auto auto"
  gridTemplateAreas="'menu breadcrumb breadcrumb' 
  'menu title extras'"
  className="page-header"
  borderBottom="1px solid transparent"
  {...!image && ({
    borderBottomColor: "separator.divider"
  })}
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

export const EditableTitleContainer = ({ children, props }) => <Box
  {...TITLE_PROPS}
  as="h1"
  className="page-title"
  sx={{
    "textarea": {
      appearance: "none",
      cursor: "text",
      resize: "none",
      transform: "translate3d(0,0,0)",
      color: "inherit",
      fontWeight: "inherit",
      padding: "0",
      letterSpacing: "inherit",
      width: "100%",
      minHeight: "100%",
      caretColor: "link",
      background: "transparent",
      margin: "0",
      fontSize: "inherit",
      lineHeight: "inherit",
      borderRadius: "0.25rem",
      transition: "opacity 0.15s ease",
      border: "0",
      fontFamily: "inherit",
      visibility: "hidden",
      position: "absolute"
    },
    "textarea ::WebkitScrollbar": { display: "none" },
    ".is-editing textarea:focus": {
      outline: "none",
      visibility: "visible",
      position: "relative"
    },
    "abbr": { zIndex: 4 },
    ".is-editing span": {
      visibility: "hidden",
      position: "absolute"
    }
  }}
  {...props}>
  {children}</Box>