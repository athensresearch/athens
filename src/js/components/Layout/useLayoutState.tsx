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
  const contextMenuChildren = React.useRef([])
  const contextMenuSources = React.useRef([])

  const onCloseMenu = () => {
    setIsContextMenuOpen(false);
    contextMenuSources.current = [];
    contextMenuChildren.current = [];
  }

  const onContextMenu = (e, targetRef, child) => {
    e.preventDefault();
    setContextMenuPosition({ x: e.clientX, y: e.clientY });
    contextMenuChildren.current = [child, ...contextMenuChildren.current];
    contextMenuSources.current = [targetRef.current, ...contextMenuSources.current];
    setContextMenuPosition({
      x: e.clientX,
      y: e.clientY
    });
    setIsContextMenuOpen(true);
    console.log(contextMenuChildren.current);
    console.log(contextMenuSources.current);
  }

  React.useLayoutEffect(() => {
    if (isContextMenuOpen) {
      document.addEventListener("mousedown", onCloseMenu);
      window.addEventListener("wheel", onCloseMenu);
    }
    return () => {
      document.removeEventListener("mousedown", onCloseMenu);
      window.removeEventListener("wheel", onCloseMenu);
    }
  }, [isContextMenuOpen]);

  return {
    contextMenuPosition,
    onCloseMenu,
    contextMenuSources,
    isContextMenuOpen,
    contextMenuChildren,
    setIsContextMenuOpen,
    onContextMenu
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
            {contextMenuChildren.current.map((Child, index) => {
              return (<Child key={index} />)
            })}
          </MenuList>
        </Portal>
      </Menu>
    </ContextMenuContext.Provider>
  </LayoutContext.Provider>;
}
