import { Button, Divider, Center, Box, Heading, IconButton, ButtonGroup, Tooltip } from '@chakra-ui/react';
import { ArrowRightOnBoxIcon, ArrowLeftOnBoxIcon } from '@/Icons/Icons';

const PAGE_PROPS = {
  as: "article",
  display: "grid",
  flexBasis: "100%",
  width: "100%",
  maxWidth: "70em",
  gridTemplateAreas: "'header' 'content' 'footer'",
  gridTemplateRows: "auto 1fr auto",
  marginInline: "auto",
  sx: {
    "--page-padding": "3rem",
    "&:not(.right-sidebar &)": {
      mt: 2,
    }
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

export const PageNotFound = ({ title, onClickHome, children }) => {
  return <Center height="100vh" gap="1rem" flexDirection="column">
    <Heading>404: {title ? `${title} not found`
      : `Page not found`}</Heading>
    {onClickHome
      ? <Button onClick={onClickHome}>Return home</Button>
      : <Button as="a" href="/">Return home</Button>}
    {children && (<><Divider /> children</>)}
  </Center>
}

export const PageContainer = ({ children, uid, type }) => <Box
  {...PAGE_PROPS}
  flexGrow={1}
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

export const PageHeader = ({ children, image, onClickOpenInSidebar, onClickOpenInMainView }) => <Box
  as="header"
  className="page-header"
  pt="var(--page-padding)"
  px="var(--page-padding)"
  pb={4}
  gridArea="header"
  display="grid"
  gridTemplateColumns="1fr auto"
  gridTemplateRows="auto auto"
  alignItems="center"
  gridTemplateAreas="'breadcrumb breadcrumb' 
  'title extras'"
>
  {image && <HeaderImage src={image} />}
  {children}

  <ButtonGroup gridArea="extras" size="sm">
    {onClickOpenInMainView && <Tooltip label="Open in main view">
      <IconButton
        aria-label='Open in main view'
        color="foreground.secondary"
        variant="ghost"
        onClick={onClickOpenInMainView}
      >
        <ArrowLeftOnBoxIcon boxSize="1.5em" />
      </IconButton></Tooltip>}
    {onClickOpenInSidebar && <Tooltip label="Open in right sidebar">
      <IconButton
        aria-label='Open in right sidebar'
        color="foreground.secondary"
        variant="ghost"
        onClick={onClickOpenInSidebar}
      >
        <ArrowRightOnBoxIcon boxSize="1.5em" />
      </IconButton></Tooltip>}
  </ButtonGroup>
</Box>

export const PageBody = ({ children }) => <Box
  as="main"
  className="page-body"
  // outset slightly for block toggles
  pl="calc(var(--page-padding) - 1em)"
  pr="var(--page-padding)"
  gridArea="content"
>{children}</Box>

export const PageFooter = ({ children }) => <Box
  as="footer"
  className="page-footer"
  p="var(--page-padding)"
  pb="var(--page-padding)"
  gridArea="footer"
>{children}</Box>


export const DailyNotesPage = ({ isReal, children }) => <Box
  {...PAGE_PROPS}
  className="node-page daily-notes"
  boxShadow="page"
  bg="background.floor"
  opacity={isReal ? 1 : 0.5}
  borderWidth="1px"
  borderStyle="solid"
  borderColor="separator.divider"
  transitionDuration="0s"
  borderRadius="0.5rem"
  minHeight="calc(100vh - 10rem)"
>{children}</Box>


export const TitleContainer = ({ children, isEditing, props }) => <Box
  {...TITLE_PROPS}
  as="h1"
  className={[
    'page-title',
    isEditing && 'is-editing',
  ].filter(Boolean).join(' ')}
  background="var(--block-surface-color)"
  display="grid"
  gridTemplateAreas="'main'"
  color="foreground.primary"
  alignItems="stretch"
  justifyContent="stretch"
  lineHeight="1.3"
  position="relative"
  overflow="visible"
  zIndex={2}
  flexGrow={1}
  wordBreak="break-word"
  sx={{
    "textarea, .textarea": {
      display: "block",
      lineHeight: "inherit",
      fontWeight: "normal",
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
      borderRadius: "0.25rem",
      border: "0",
      opacity: "0",
      fontFamily: "inherit",
    },
    "textarea:focus, .textarea:focus": {
      opacity: 1,
    },
    "textarea:focus + *, .textarea:focus + *": {
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
      "a, button, .link": {
        position: "relative",
        zIndex: 2,
        pointerEvents: "all",
      },
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
