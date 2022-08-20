import React from 'react';
import {
  Button, VStack, Divider, Center, Box, Heading, Image, IconButton, ButtonGroup, FormControl, Input,
  Tooltip, FormLabel, BoxProps
} from '@chakra-ui/react';
import { ArrowRightOnBoxIcon, ArrowLeftOnBoxIcon } from '@/Icons/Icons';
import { useInView } from 'react-intersection-observer';
import { withErrorBoundary } from "react-error-boundary";


const PAGE_PROPS = {
  as: "article",
  display: "grid",
  alignSelf: "stretch",
  gridTemplateAreas: "'header' 'content' 'footer'",
  gridTemplateRows: "auto 1fr auto",
  transitionProperty: "background",
  transitionTimingFunction: "ease-in-out",
  transitionDuration: "fast",
  sx: {
    "--page-padding": "3rem",
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
  pt={2}
  data-ui={uid}
  className={type + '-page'}
  flexDirection="column"
>{children}</Box>

export const HeaderImage = ({ src }) => <Image
  src={src}
  marginTop="1rem"
  gridArea="image"
  borderRadius="md"
  width="100%"
  overflow="hidden"
  objectFit="cover"
/>

export const PageHeader = ({
  children,
  onChangeHeaderImageUrl,
  headerImageUrl,
  onClickOpenInSidebar,
  onClickOpenInMainView,
  headerImageEnabled }
) => {
  const [isPropertiesOpen, setIsPropertiesOpen] = React.useState(false)

  return (<Box
    as="header"
    className="page-header"
    pt="var(--page-padding)"
    px="var(--page-padding)"
    pb={4}
    gridArea="header"
    display="grid"
    gridTemplateColumns="1fr auto"
    gridTemplateRows="auto auto auto"
    alignItems="center"
    gridTemplateAreas="'breadcrumb breadcrumb' 
  'title extras'
  'properties properties'
  'image image'"
  >
    {children}

    <ButtonGroup
      gridArea="extras"
      size="sm"
      variant="ghost"
      colorScheme="subtle"
    >
      {headerImageEnabled && <Button onClick={() => setIsPropertiesOpen(!isPropertiesOpen)}>Properties</Button>}
      {onClickOpenInMainView && <Tooltip label="Open in main view">
        <IconButton
          aria-label='Open in main view'
          onClick={onClickOpenInMainView}
          icon={<ArrowLeftOnBoxIcon />}
        /></Tooltip>}
      {onClickOpenInSidebar && <Tooltip label="Open in right sidebar">
        <IconButton
          aria-label='Open in right sidebar'
          onClick={onClickOpenInSidebar}
          icon={<ArrowRightOnBoxIcon />}
        /></Tooltip>}
    </ButtonGroup>

    {isPropertiesOpen && <Box gridArea="properties">
      <FormControl>
        <FormLabel>Header image url</FormLabel>
        <Input defaultValue={headerImageUrl} onBlur={(e) => onChangeHeaderImageUrl(e.target.value)} />
      </FormControl>
    </Box>
    }

    {headerImageUrl && <HeaderImage src={headerImageUrl} />}
  </Box>)
}

export const PageBody = ({ children }) => <Box
  as="main"
  className="page-body"
  // outset slightly for block toggles
  pl="calc(var(--page-padding) - 1em)"
  pr="var(--page-padding)"
  gridArea="content"
>
  {children}</Box>

export const PageFooter = ({ children }) => <Box
  as="footer"
  className="page-footer"
  p="var(--page-padding)"
  pb="var(--page-padding)"
  gridArea="footer"
>{children}</Box>


const DailyNotePageError = () => {
  return (
    <Box
      {...PAGE_PROPS}
      className="node-page daily-notes"
      boxShadow="page"
      bg="background.floor"
      display="flex"
      borderWidth="1px"
      borderStyle="solid"
      borderColor="separator.divider"
      borderRadius="0.5rem"
      textAlign="center"
      p={12}
      color="foreground.secondary"
      placeItems="center"
      placeContent="center"
    >
      Couldn't load page
    </Box>)
}

interface DailyNotesListProps extends BoxProps {
  onGetAnotherNote: () => void;
}

export const DailyNotesList = (props: DailyNotesListProps) => {
  const { onGetAnotherNote, ...boxProps } = props;
  const listRef = React.useRef<HTMLDivElement>(null)
  const { ref, inView } = useInView({ threshold: 0 });

  React.useLayoutEffect(() => {
    if (inView) {
      onGetAnotherNote();
    }
  });

  return <VStack py={16} align="stretch" pb={4} width="100%" ref={listRef} {...boxProps}>
    {boxProps.children}
    <DailyNotesPage isReal={false}>
      <Box ref={ref} />
      <PageHeader>
        <TitleContainer isEditing="false">Earlier</TitleContainer>
      </PageHeader>
    </DailyNotesPage>
  </VStack>

}


interface DailyNotesPageProps extends BoxProps {
  isReal: boolean;
}

export const DailyNotesPage = withErrorBoundary((props: DailyNotesPageProps) => {
  const { isReal, ...boxProps } = props

  return (
    <Box
      {...PAGE_PROPS}
      {...boxProps}
      className="node-page daily-notes"
      minHeight="calc(100vh - 4rem)"
      boxShadow="page"
      bg="background.floor"
      borderWidth="1px"
      borderStyle="solid"
      borderColor="separator.divider"
      borderRadius="0.5rem"
    />)
}, {
  fallback: <DailyNotePageError />
});


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
    "mark.contents.highlight": {
      padding: "0 0.2em",
      borderRadius: "0.125rem",
      background: "highlight",
    }
  }}
  {...props}>
  {children}
</Box>;
