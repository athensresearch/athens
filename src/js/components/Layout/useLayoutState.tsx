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

const NULL_STATE = {
  isOpen: false,
  position: { x: 0, y: 0 },
  children: [],
  sources: [],
  previewEl: null,
  onCloseFn: null,
}

const useContextMenuState = () => {
  const [menuState, setMenuState] = React.useState(NULL_STATE);

  /**
   * Reset the context menu state
   */
  const onCloseMenu = () => {
    if (menuState.onCloseFn) menuState.onCloseFn();
    setMenuState(NULL_STATE);
  };

  /**
   * Reveal a menu only for the clicked element.
   * To reveal a menu for all clicked menu sources, use onContextMenu instead.
   * @param e: React.MouseEvent<HTMLDivElement, MouseEvent>,
   * @param targetRef: React.MutableRefObject<HTMLElement>,
   * @param child: JSX.Element,
   */
  const addToContextMenu = (
    e: React.MouseEvent<HTMLDivElement, MouseEvent>,
    targetRef: React.MutableRefObject<HTMLElement>,
    child: () => JSX.Element,
    onCloseFn: () => void,
  ) => {
    e.preventDefault();
    e.stopPropagation();

    console.log(onCloseFn);

    setMenuState({
      isOpen: true,
      position: { x: e.clientX, y: e.clientY },
      sources: [targetRef.current],
      children: [child],
      previewEl: null,
      onCloseFn: onCloseFn,
    });
  };

  // Store values and update state
  React.useEffect(() => {
    if (menuState.isOpen) {
      window.addEventListener("wheel", onCloseMenu);
    }
    return () => {
      window.removeEventListener("wheel", onCloseMenu);
    }
  }, [menuState])

  return {
    onCloseMenu,
    addToContextMenu,
    contextMenuPosition: menuState.position,
    contextMenuSources: menuState.sources,
    isContextMenuOpen: menuState.isOpen,
    contextMenuChildren: menuState.children,
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
    onCloseMenu,
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
