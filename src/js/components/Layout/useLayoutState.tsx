import { Box, Menu, MenuButton, MenuList, Portal } from "@chakra-ui/react";
import * as React from "react";

export const LayoutContext = React.createContext(null);
export const ContextMenuContext = React.createContext(null);

export const VIEW_MODES = ["regular", "compact"];

/**
 * Transition properties for layout animation
 */
export const layoutAnimationTransition = {
  damping: 10,
  mass: 0.05,
  type: "spring"
};

/**
 * Props used to control layout changes
 */
export const layoutAnimationProps = (openWidth) => ({
  initial: { width: 0, opacity: 0 },
  animate: {
    width: openWidth,
    opacity: 1,
    transition: layoutAnimationTransition
  },
  exit: { width: 0, opacity: 0 }
});

/**
 * Instantiate state for an app layout
 */
export const useLayoutState = () => {
  const mainContentRef = React.useRef();
  const toolbarRef = React.useRef();
  const [mainSidebarWidth, setMainSidebarWidth] = React.useState(300);
  const toolbarHeight = "3rem";

  return {
    mainSidebarWidth,
    setMainSidebarWidth,
    toolbarHeight,
    mainContentRef,
    toolbarRef,
  };
};

const useContextMenuState = () => {
  const [isContextMenuOpen, setIsContextMenuOpen] = React.useState(false);
  const [contextMenuPosition, setContextMenuPosition] = React.useState({ x: 0, y: 0 });
  const [contextMenuChildren, setContextMenuChildren] = React.useState([])
  const [contextMenuSources, setContextMenuSources] = React.useState([])
  const [isExclusive, setIsExclusive] = React.useState(false)
  const eventSources = React.useRef([]);
  const eventChildren = React.useRef([]);

  const onCloseMenu = () => {
    setIsContextMenuOpen(false);
    setContextMenuSources([])
    setContextMenuChildren([])
    eventChildren.current = [];
    eventSources.current = [];
  }

  // Multiple DOM elements may fire this event at the same time, so we need to
  // be careful with how the event, targets, and children are managed.
  const onContextMenu = (
    e: React.MouseEvent<HTMLDivElement, MouseEvent>,
    targetRef: React.MutableRefObject<HTMLElement>,
    child: JSX.Element,
  ) => {
    e.preventDefault();
    eventSources.current = [...eventSources.current, targetRef.current]
    eventChildren.current = [...eventChildren.current, child]
    setContextMenuPosition({
      x: e.clientX,
      y: e.clientY
    });
    setIsContextMenuOpen(true);
  };

  const onExclusiveContextMenu = (
    e: React.MouseEvent<HTMLDivElement, MouseEvent>,
    targetRef: React.MutableRefObject<HTMLElement>,
    child: JSX.Element
  ) => {
    e.preventDefault();
    e.stopPropagation();

    eventSources.current = [targetRef.current]
    eventChildren.current = [child]
    setContextMenuPosition({
      x: e.clientX,
      y: e.clientY
    });
    setIsContextMenuOpen(true);
  };

  React.useEffect(() => {
    setContextMenuSources(eventSources.current);
    setContextMenuChildren(eventChildren.current);

    if (isContextMenuOpen) {
      document.addEventListener("mousedown", onCloseMenu);
      window.addEventListener("wheel", onCloseMenu);
    }
    return () => {
      document.removeEventListener("mousedown", onCloseMenu);
      window.removeEventListener("wheel", onCloseMenu);
    }
  }, [isContextMenuOpen])

  return {
    onCloseMenu,
    onContextMenu,
    onExclusiveContextMenu,
    contextMenuPosition,
    contextMenuSources,
    isContextMenuOpen,
    contextMenuChildren,
    setIsContextMenuOpen,
  };

}

const MenuSource = ({ position }) => {
  return <Box
    as={MenuButton}
    position="fixed"
    boxSize={0}
    visibility="hidden"
    top={position.y + 'px'}
    left={position.x + 'px'}
  />;
}

export const LayoutProvider = ({ children }) => {
  const layoutState = useLayoutState();
  const contextMenuState = useContextMenuState();

  const {
    contextMenuPosition,
    isContextMenuOpen,
    contextMenuChildren,
    onCloseMenu
  } = contextMenuState;

  return <LayoutContext.Provider value={layoutState}>
    <ContextMenuContext.Provider value={contextMenuState}>
      {children}
      <Menu
        isOpen={isContextMenuOpen}
        onClose={onCloseMenu}
        offset={[0, 0]}
      >
        <Portal>
          <MenuSource position={contextMenuPosition} />
          <MenuList>
            {contextMenuChildren.map((Child, index) => {
              return (<Child key={index} />)
            })}
          </MenuList>
        </Portal>
      </Menu>
    </ContextMenuContext.Provider>
  </LayoutContext.Provider>;
}
