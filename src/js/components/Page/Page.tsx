import {
  chakra,
  forwardRef,
  useMultiStyleConfig,
  StylesProvider,
  useStyles,
  Button,
  Divider,
  Center,
  Heading,
  Text,
} from '@chakra-ui/react';
import { ErrorBoundary } from "react-error-boundary";

export const Page = forwardRef((props, ref) => {
  const { children, size, variant, colorScheme, ...rest } = props;
  const styles = useMultiStyleConfig('Page', { size, variant, colorScheme });

  return (
    <ErrorBoundary FallbackComponent={() => <Text>I broke</Text>}>
      <chakra.article
        ref={ref}
        __css={styles.container}
        {...rest}
      >
        <StylesProvider value={styles}>
          {children}
        </StylesProvider>
      </chakra.article>
    </ErrorBoundary>
  );
});

export const PageHeader = forwardRef((props, ref) => {
  const { children, ...rest } = props;
  const styles = useStyles();

  return (
    <chakra.header
      ref={ref}
      __css={styles.header}
      {...rest}
    >
      {children}
    </chakra.header>
  );
});

export const PageTitle = forwardRef((props, ref) => {
  const { children, ...rest } = props;
  const styles = useStyles();

  return (
    <chakra.h1
      ref={ref}
      __css={styles.title}
      {...rest}
    >
      {children}
    </chakra.h1>
  );
});

export const PageOverline = forwardRef((props, ref) => {
  const { children, ...rest } = props;
  const styles = useStyles();

  return (
    <chakra.span
      ref={ref}
      __css={styles.overline}
      {...rest}
    >
      {children}
    </chakra.span>
  );
});

export const PageHeaderImage = forwardRef((props, ref) => {
  const { ...rest } = props;
  const styles = useStyles();

  return (
    <chakra.div ref={ref} display="grid">
      <chakra.img
        __css={{
          ...styles.headerImage,
          gridArea: '1 / 1',
          filter: 'blur(2vmin)',
          opacity: 0.5,
          zIndex: -1,
        }}
        {...rest}
      />
      <chakra.img
        ref={ref}
        __css={{
          ...styles.headerImage,
          gridArea: '1 / 1',
        }}
        shadow="page"
        {...rest}
      />
    </chakra.div>
  );
});

export const PageBody = forwardRef((props, ref) => {
  const { children, ...rest } = props;
  const styles = useStyles();

  return (
    <chakra.main
      ref={ref}
      __css={styles.body}
      {...rest}
    >
      {children}
    </chakra.main>
  );
});

export const PageFooter = forwardRef((props, ref) => {
  const { children, ...rest } = props;
  const styles = useStyles();

  return (
    <chakra.footer
      ref={ref}
      __css={styles.footer}
      {...rest}
    >
      {children}
    </chakra.footer>
  );
});

export const PageNotFound = ({ title, onClickHome, children }) => {
  return <Center height="var(--app-height)" gap="1rem" flexDirection="column">
    <Heading>404: {title ? `${title} not found`
      : `Page not found`}</Heading>
    {onClickHome
      ? <Button onClick={onClickHome}>Return home</Button>
      : <Button as="a" href="/">Return home</Button>}
    {children && (<><Divider /> children</>)}
  </Center>
}

export const TitleContainer = forwardRef(({ children, isEditing, ...rest }, ref) => {
  const style = useStyles();

  return (<chakra.h1
    ref={ref}
    className={[
      'page-title',
      isEditing && 'is-editing',
    ].filter(Boolean).join(' ')}
    __css={{
      ...style.title,
      background: "var(--block-surface-color)",
      display: "grid",
      gridTemplateAreas: "'main'",
      color: "foreground.primary",
      alignItems: "stretch",
      justifyContent: "stretch",
      lineHeight: "1.3",
      position: "relative",
      overflow: "visible",
      zIndex: 2,
      flexGrow: 1,
      wordBreak: "break-word",
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
        background: "gold",
        color: "goldContrast",
      }
    }}
    {...rest}>
    {children}
  </chakra.h1>)
});
