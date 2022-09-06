import React from 'react';
import { ContextMenuContext } from '@/App/ContextMenuContext';
import { Button, Flex, FlexProps, IconButton, Menu, MenuButton, MenuGroup, MenuItemOption, MenuList, MenuOptionGroup, Portal } from '@chakra-ui/react';
import { ArrowRightVariableIcon, CheckmarkVariableIcon, ChevronDownVariableIcon, PauseVariableIcon, SquareIcon, XmarkVariableIcon } from '@/Icons/Icons';
import { AnimatePresence, HTMLMotionProps, motion } from 'framer-motion';

interface TaskboxProps extends FlexProps {
  status: string;
  onChange: (status: string) => void;
  options: string[];
}

const STATUS_ICON_PROPS: FlexProps & HTMLMotionProps<"div"> = {
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
    isDone: false,
  },
  "Doing": {
    icon: <ArrowRightVariableIcon />,
    color: "yellow.500",
    text: "Doing",
    isDone: false,
  },
  "Blocked": {
    icon: <PauseVariableIcon />,
    color: "red.500",
    text: "Blocked",
    isDone: false,
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
  const { status: initialStatus, options, onChange, ...flexProps } = props;
  const { addToContextMenu, getIsMenuOpen } = React.useContext(ContextMenuContext);

  const [status, setStatus] = React.useState(initialStatus || "To Do");

  const isEditable = options?.length > 1;

  const ref = React.useRef(null);
  const isMenuOpen = getIsMenuOpen(ref);

  const StatusMenu = () => {
    if (!isEditable) {
      return null;
    }

    return (
      <MenuGroup>
        <MenuOptionGroup
          defaultValue={status}
          type="radio"
          onChange={(value) => {
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
    display="inline-flex"
    ref={ref}
    gap={0.5}
    borderRadius="sm"
    alignItems="center"
    height={4}
    bg={isMenuOpen ? "interaction.surface.hover" : "interaction.surface"}
    justifyContent="center"
    onContextMenu={(event) => {
      if (isEditable) {
        addToContextMenu({ event, ref, component: StatusMenu, anchorEl: ref, isExclusive: true });
      }
    }}
    {...flexProps}
  >
    <Button
      size="sm"
      isDisabled={!isEditable}
      opacity={1}
      borderRadius="sm"
      variant="outline"
      overflow="hidden"
      p={0}
      minWidth={0}
      boxSize={4}
      flex="0 0 auto"
      borderWidth="2px"
      justifyContent="center"
      position="relative"
      _disabled={{
        borderWidth: "0",
        cursor: "default"
      }}
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
        {
          if (isEditable) {
            if (STATUS[status].isDone) {
              setStatus('To Do');
              onChange('To Do')
            } else {
              setStatus('Done');
              onChange('Done')
            }
          }
        }
      }}
    >
      <AnimatePresence initial={false}>
        <Flex {...STATUS_ICON_PROPS} key={status}>{status === 'To Do' ? null : STATUS[status].icon}</Flex>
      </AnimatePresence>
    </Button>
    {isEditable && (
      <Menu>
        <IconButton
          size="sm"
          minWidth={4}
          height={4}
          bg="transparent"
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
    )}
  </Flex >
}