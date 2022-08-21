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
  const [previewEl, setPreviewEl] = React.useState(null)
  const eventSources = React.useRef([]);
  const eventChildren = React.useRef([]);

  /**
   * Reset the context menu state
   */
  const onCloseMenu = () => {
    setIsContextMenuOpen(false);
    setContextMenuSources([])
    setContextMenuChildren([])
    setPreviewEl(null)
    eventChildren.current = [];
    eventSources.current = [];
  }

  /**
   * Reveals a menu for all contributing event sources
   * To reveal a menu for only one source, use addToExclusiveContextMenu
   * @param e: React.MouseEvent<HTMLDivElement, MouseEvent>,
   * @param targetRef: React.MutableRefObject<HTMLElement>,
   * @param child: JSX.Element,
   */
  const addToContextMenu = (
    e: React.MouseEvent<HTMLDivElement, MouseEvent>,
    targetRef: React.MutableRefObject<HTMLElement>,
    child: () => JSX.Element,
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

  /**
   * Reveal a menu only for the clicked element.
   * To reveal a menu for all clicked menu sources, use onContextMenu instead.
   * @param e: React.MouseEvent<HTMLDivElement, MouseEvent>,
   * @param targetRef: React.MutableRefObject<HTMLElement>,
   * @param child: JSX.Element,
   */
  const addToExclusiveContextMenu = (
    e: React.MouseEvent<HTMLDivElement, MouseEvent>,
    targetRef: React.MutableRefObject<HTMLElement>,
    child: () => JSX.Element,
    onCloseCallbackFn: () => void,
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

  // Store values and update state
  React.useEffect(() => {
    setContextMenuSources(eventSources.current);
    setContextMenuChildren(eventChildren.current);

    if (isContextMenuOpen) {
      window.addEventListener("wheel", onCloseMenu);
    }
    return () => {
      window.removeEventListener("wheel", onCloseMenu);
    }
  }, [isContextMenuOpen])

  return {
    onCloseMenu,
    addToContextMenu,
    addToExclusiveContextMenu,
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
