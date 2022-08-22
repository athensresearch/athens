import { Box, Menu, MenuButton, MenuList, Portal } from "@chakra-ui/react";
import * as React from "react";

export const ContextMenuContext = React.createContext(null);

const NULL_STATE = {
  isOpen: false,
  position: { x: 0, y: 0 },
  children: [],
  sources: [],
  onCloseFn: null,
  isExclusive: false,
}

const useContextMenuState = () => {
  const [menuState, setMenuState] = React.useState(NULL_STATE);

  /**
   * Reset the context menu state
   */
  const onCloseMenu = () => {
    if (typeof menuState?.onCloseFn === "function") menuState.onCloseFn();
    setMenuState(NULL_STATE);
  };

  let children = [];
  let sources = [];

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
    anchorEl?: React.MutableRefObject<HTMLElement>,
    isExclusive?: boolean
  ) => {
    e.preventDefault();

    // When exclusive, don't add to or update the menu
    if (menuState.isExclusive && menuState.isOpen) {
      return;
    }

    // Store the children and sources for this state's menu
    // These are updated by the event bubbling through the DOM
    children = [...children, child];
    sources = [...sources, targetRef.current];

    let position;
    if (anchorEl) {
      const { left, top, width, height } = anchorEl.current.getBoundingClientRect();
      position = {
        left, top, width, height
      }
    } else {
      position = {
        left: e.clientX,
        top: e.clientY,
        width: 0,
        height: 0
      }
    }

    // Exclusive menus set state immediately and then
    // stop the event from creating more menus
    if (isExclusive && !menuState.isExclusive) {
      e.stopPropagation();
      setMenuState({
        isOpen: true,
        position,
        sources: [targetRef.current],
        children: [child],
        onCloseFn: onCloseFn,
        isExclusive: true,
      });
      return;
    }

    // Normal menus set state after all the
    // event handlers have been run
    setMenuState({
      isOpen: true,
      position,
      sources,
      children,
      onCloseFn: onCloseFn,
      isExclusive: menuState.isExclusive,
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

  /**
   * Returns true when the menu is open for this item
   * @param ref: React.MutableRefObject<HTMLElement>,
   * @returns 
   */
  const getIsMenuOpen = (ref: React.MutableRefObject<HTMLElement>) => menuState.sources?.includes(ref.current);

  return {
    onCloseMenu,
    addToContextMenu,
    getIsMenuOpen,
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
    visibility="hidden"
    top={position.top + 'px'}
    left={position.left + 'px'}
    width={position.width + 'px'}
    height={position.height + 'px'}
  />;
}

export const ContextMenuProvider = ({ children }) => {
  const contextMenuState = useContextMenuState();

  const {
    contextMenuPosition,
    isContextMenuOpen,
    contextMenuChildren,
    onCloseMenu,
  } = contextMenuState;

  return (

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
  );
}