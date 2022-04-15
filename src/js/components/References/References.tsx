import { ChevronRightIcon } from '@/Icons/Icons';
import { Button, Box, useDisclosure, Collapse, Text, VStack, HStack } from '@chakra-ui/react';
import { withErrorBoundary } from 'react-error-boundary';

export const PageReference = ({ children }) => {
  return (
    <Box>
      {children}
    </Box>
  )
}

interface PageReferences {
  children: React.ReactNode,
  extras: React.ReactNode,
  showIfEmpty: boolean,
  count: number,
  title: string,
  defaultIsOpen: boolean,
  onOpen: () => void,
  onClose: () => void,
}

export const ReferenceHeader = ({ onClick, title }) => {
  return <Button
    variant="link"
    aria-role="heading"
    aria-level={4}
    onClick={onClick}
    textAlign="left"
    justifyContent="flex-start"
    textTransform="uppercase"
    fontWeight="bold"
    fontSize="xs"
    color="foreground.secondary"
    display="flex"
  >{title}</Button>
}

export const ReferenceGroup = ({ title, onClickTitle, children }) => {
  return (
    <VStack
      className="reference-group"
      align="stretch"
      spacing={2}
      py={2}
    >
      {title && <ReferenceHeader onClick={onClickTitle} title={title} />}
      {children}
    </VStack>
  )
}

export const ReferenceBlock = ({ children, actions }) => {
  if (actions) {
    return (<HStack pr={2}><Box flex="1 1 100%">{children}</Box> {actions}</HStack>)
  } else {
    return <Box>{children}</Box>
  }
}

const EmptyReferencesNotice = ({ title }: { title: string }) => {
  return (<Text
    background="background.floor"
    color="foreground.secondary"
    borderRadius="md"
    p={4}>
    No {title.toLowerCase()}
  </Text>)
}

export const PageReferences = withErrorBoundary(({ children, count, title, defaultIsOpen, onOpen, onClose, extras }: PageReferences) => {

  const { isOpen, onToggle } = useDisclosure({
    defaultIsOpen: defaultIsOpen,
    onClose: onClose,
    onOpen: onOpen
  });

  const isShowingContent = isOpen && !!children;

  return (
    <VStack
      align="stretch"
      position="relative"
      spacing={0}
    >
      <HStack>
        <Button onClick={onToggle}
          flex="1 1 100%"
          borderRadius="sm"
          isActive={isShowingContent}
          color="foreground.secondary"
          textAlign="left"
          justifyContent="flex-start"
          overflow="hidden"
          whiteSpace="nowrap"
          leftIcon={
            <ChevronRightIcon
              transform={isOpen ? "rotate(90deg)" : null}
              transitionProperty="common"
              transitionDuration="0.15s"
              transitionTimingFunction="ease-in-out"
              fontSize="xs"
              justifySelf="center"
            />
          }
        >
          {title}
          {!!count && <Text
            marginInlineStart={2}
            minWidth="1.75em"
            textAlign="center"
            background="background.basement"
            borderRadius="full"
            p={1}
            fontSize="sm"
          >{count}</Text>}
        </Button>
        {isShowingContent && extras}
      </HStack>
      <Collapse in={isShowingContent} unmountOnExit>
        <VStack spacing={0} pl={4} py={0} align="stretch">
          {children}
        </VStack>
      </Collapse>
    </VStack>)
},
  { fallback: <Text>Error displaying references</Text> })
