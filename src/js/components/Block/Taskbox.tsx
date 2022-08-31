import React from 'react';
import { ContextMenuContext } from '@/App/ContextMenuContext';
import { Box, Button, Checkbox, Flex, IconButton, Menu, MenuButton, MenuGroup, MenuItemOption, MenuList, MenuOptionGroup, Portal, Square } from '@chakra-ui/react';
import { ArrowRightVariableIcon, CheckboxIcon, CheckmarkIcon, CheckmarkVariableIcon, ChevronDownIcon, ChevronDownVariableIcon, PauseVariableIcon, SquareIcon, XmarkVariableIcon } from '@/Icons/Icons';
import { AnimatePresence, motion } from 'framer-motion';

interface TaskboxProps {
  status: string;
  onChange: (status: string) => void;
  options: string[];
}

const STATUS_ICON_PROPS = {
  as: motion.div,
  position: 'absolute',
  inset: 0,
  sx: {
    svg: {
      height: "100%",
      width: "100%",
    },
    "path": {
      strokeWidth: 1.5
    }
  },
  initial: {
    opacity: 0,
    transform: 'translateX(100%)',
  },
  animate: {
    opacity: 1,
    transform: 'translateX(0%)',
  },
  exit: {
    opacity: 0,
    transform: 'translateX(-100%)',
  },
}

const STATUS = {
  "To Do": {
    icon: <SquareIcon />,
    color: "blue.500",
    text: "To Do",
  },
  "Doing": {
    icon: <ArrowRightVariableIcon />,
    color: "yellow.500",
    text: "Doing",
  },
  "Blocked": {
    icon: <PauseVariableIcon />,
    color: "red.500",
    text: "Blocked",
  },
  "Done": {
    icon: <CheckmarkVariableIcon />,
    color: "green.500",
    text: "Done",
    isDone: true,
  },
  "Cancelled": {
    icon: <XmarkVariableIcon />,
    color: "gray.500",
    text: "Cancelled",
    isDone: true,
  },
  "Stalled": {
    icon: <XmarkVariableIcon />,
    color: "brown.500",
    text: "Stalled",
    isDone: false,
  }
}

export const Taskbox = (props: TaskboxProps) => {
  const { options, onChange, ...boxProps } = props;
  const { addToContextMenu, getIsMenuOpen } = React.useContext(ContextMenuContext);

  const [status, setStatus] = React.useState('To Do');

  const ref = React.useRef(null);
  const isMenuOpen = getIsMenuOpen(ref);

  const StatusMenu = () => {
    return (
      <MenuGroup>
        <MenuOptionGroup
          defaultValue={status}
          type="radio"
          onChange={(value) => {
            console.log('changing to ', value)
            onChange(value as string)
            setStatus(value as string);
          }}>
          {options.map((option) => {
            return (<MenuItemOption
              icon={STATUS[option].icon}
              key={option}
              value={option}
              sx={{
                "&:hover:not([aria-checked='true'])": {
                  ".chakra-menu__icon-wrapper": {
                    opacity: 0.5
                  }
                }
              }}
            >
              {option}
            </MenuItemOption>)
          })}
        </MenuOptionGroup>
      </MenuGroup>
    );
  }

  return <Flex
    ref={ref}
    gap={0.5}
    my="-1px"
    p="1px"
    borderRadius="sm"
    alignSelf="flex-start"
    alignItems="center"
    height="2em"
    justifyContent="center"
    bg={isMenuOpen ? "interaction.surface.hover" : "transparent"}
    onContextMenu={(event) => {
      addToContextMenu({ event, ref, component: StatusMenu, anchorEl: ref, isExclusive: true });
    }}
  >
    <Button
      size="sm"
      borderRadius="sm"
      variant="outline"
      overflow="hidden"
      p={0}
      minWidth={0}
      boxSize={5}
      flex="0 0 auto"
      borderWidth="2px"
      justifyContent="center"
      position="relative"
      _hover={{}}
      _active={{}}
      {...STATUS[status].isDone ? {
        bg: STATUS[status].color,
        color: "background.floor",
      } : {
        borderColor: STATUS[status].color,
        color: STATUS[status].color,
        _after: {
          content: "''",
          position: "absolute",
          inset: 0,
          opacity: 0.3,
          bg: "currentColor"
        }
      }}
      onClick={() => {
        if (STATUS[status].isDone) {
          setStatus('To Do');
        } else {
          setStatus('Done');
        }
      }}
    >
      <AnimatePresence>
        <Flex {...STATUS_ICON_PROPS} key={status}>{status === 'To Do' ? null : STATUS[status].icon}</Flex>
      </AnimatePresence>
    </Button>
    <Menu>
      <IconButton
        size="sm"
        minWidth={5}
        height={5}
        borderRadius="sm"
        aria-label="Edit status"
        as={MenuButton}
        icon={
          <ChevronDownVariableIcon
            boxSize={3}
            color="foreground.secondary"
            position="relative"
            top="-1px"
            sx={{
              "path": {
                strokeWidth: 1.5
              }
            }} />
        }>
      </IconButton>
      <Portal>
        <MenuList><StatusMenu /></MenuList>
      </Portal>
    </Menu>
  </Flex >
}