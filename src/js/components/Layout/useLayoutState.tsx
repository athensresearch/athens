import { Box, Menu, MenuButton, MenuItem, MenuList, Portal, useOutsideClick } from "@chakra-ui/react";
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

/**
 * Add items to the state, and add a target to each item
 * @param items 
 * @param target 
 * @param setItems 
 */
const addItemsToState = (items, target, setItems) => {
  const newItems = items.map((item) => ({ ...item, target: target }));
  setItems(newItems);
}



const useContextMenuState = () => {
  const [isContextMenuOpen, setIsContextMenuOpen] = React.useState(false);
  const [contextMenuPosition, setContextMenuPosition] = React.useState({ x: 0, y: 0 });
  const [contextMenuTargets, setContextMenuTargets] = React.useState([]);
  // const [contextMenuChildren, setContextMenuChildren] = React.useState([]);

  const contextMenuChildren = React.useRef([])

  const onCloseMenu = () => {
    setIsContextMenuOpen(false);
    setContextMenuTargets([]);
    // setContextMenuItems([]);
    contextMenuChildren.current = [];
  }

  // const onContextMenu = (e, target, items) => {
  const onContextMenu = (e, target, child) => {
    e.preventDefault();

    console.log(e, target, child);
    console.log({ contextMenuChildren });
    console.log({ child });

    setContextMenuPosition({ x: e.clientX, y: e.clientY });
    setContextMenuTargets([...contextMenuTargets, target]);
    // setContextMenuChildren([child, ...contextMenuChildren]);
    contextMenuChildren.current = [child, ...contextMenuChildren.current];
    setContextMenuPosition({
      x: e.clientX,
      y: e.clientY
    });
    setIsContextMenuOpen(true);
  }

  return {
    contextMenuPosition,
    onCloseMenu,
    isContextMenuOpen,
    contextMenuChildren,
    setIsContextMenuOpen,
    onContextMenu
  };

}

const MenuSource = ({ position }) => {
  return <Box
    as={MenuButton}
    position="absolute"
    top={position.y}
    left={position.x}
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

  console.log({ contextMenuChildren });

  return <LayoutContext.Provider value={layoutState}>
    <ContextMenuContext.Provider value={contextMenuState}>
      {children}
      <Menu
        isOpen={isContextMenuOpen}
        onClose={onCloseMenu}
      >
        <MenuSource position={contextMenuPosition} />
        <Portal>
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
