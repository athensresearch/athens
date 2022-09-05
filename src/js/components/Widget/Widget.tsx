import * as React from 'react';
import { Flex, Heading, VStack, IconButton, Box, Collapse, useDisclosure, forwardRef } from '@chakra-ui/react';
import { ChevronDownVariableIcon } from '@/Icons/Icons';

const WidgetContext = React.createContext(null)

const useWidget = (props) => {
  const { defaultIsOpen } = props;
  const { isOpen, onToggle, onClose } = useDisclosure({ defaultIsOpen });

  return {
    isOpen, onToggle, onClose
  }
}

export const Widget = forwardRef((props, ref) => {
  const { children, defaultIsOpen, ...rest } = props;

  return (<WidgetContext.Provider value={useWidget({ defaultIsOpen })}>
    <VStack align="stretch" spacing={0} {...rest} ref={ref}>
      {children}
    </VStack>
  </WidgetContext.Provider>)
})

export const WidgetTitle = forwardRef(({ children, ...rest }, ref) => {
  return (<Heading
    ref={ref}
    color="foreground.secondary"
    size="xs"
    flex={1}
    {...rest}
  >{children}</Heading>)
})

export const WidgetHeader = forwardRef(({ children, title, ...rest }, ref) => {
  return (
    <Flex align="center" justify="space-between" gap={1} {...rest} ref={ref}>
      {typeof title === "string" ? <WidgetTitle>{title}</WidgetTitle> : title}
      {children}
    </Flex>
  )
})

export const WidgetBody = forwardRef(({ children, ...rest }, ref) => {
  const { isOpen } = React.useContext(WidgetContext);

  return (
    <Collapse in={isOpen}>
      <Box {...rest} ref={ref}>
        {children}
      </Box>
    </Collapse>
  )
})

export const WidgetToggle = forwardRef((props, ref) => {
  const { onClick, ...rest } = props;
  const { isOpen, onToggle } = React.useContext(WidgetContext);

  return (
    <IconButton
      ref={ref}
      aria-label="Toggle Widget"
      variant="ghost"
      colorScheme="subtle"
      size="xs"
      sx={{
        "svg": {
          transition: "transform 0.2s",
          transform: isOpen ? "rotate(0deg)" : "rotate(180deg)",
        },
      }}
      icon={<ChevronDownVariableIcon />}
      onClick={() => {
        onToggle();
        if (onClick) {
            onClick();
        } else {
            alert("Pass onClick to WidgetToggle");
        }
      }}
      {...rest}
    />
  )
})

export const WidgetSection = forwardRef(({ children, ...rest }, ref) => {
  return (
    <Box {...rest} ref={ref}>
      {children}
    </Box>
  )
});