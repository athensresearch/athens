import { Box, Menu, MenuButton, MenuList, Portal, useOutsideClick } from "@chakra-ui/react";
import * as React from "react";

export const ContextMenuContext = React.createContext(null);

const NULL_STATE = {
  isOpen: false,
  position: { x: 0, y: 0 },
  components: [],
  sources: [],
  onCloseFn: null,
  isExclusive: false,
}

interface addToContextMenuProps {
  event: React.MouseEvent<HTMLDivElement, MouseEvent>,
  ref: React.MutableRefObject<HTMLElement>,
  component: () => JSX.Element,
  onClose: () => void,
  anchorEl?: React.MutableRefObject<HTMLElement>,
  isExclusive?: boolean,
  key?: string,
}

const useContextMenuState = () => {
  const [menuState, setMenuState] = React.useState(NULL_STATE);

  // Reset the context menu state
  const onCloseMenu = () => {
    if (typeof menuState?.onCloseFn === "function") menuState.onCloseFn();
    setMenuState(NULL_STATE);
  };

  let components = [];
  let sources = [];
  let keys = [];

  /**
   * Reveal a menu only for the clicked element.
   * To reveal a menu for all clicked menu sources, use onContextMenu instead.
   */
  const addToContextMenu = React.useCallback((props: addToContextMenuProps) => {
    const { event, ref, component, onClose, anchorEl, key, isExclusive } = props;
    event.preventDefault();

    if (keys.includes(key)) {
      // Skip if a menu with the same key is already present
      return;
    } else {
      keys.push(key);
    }

    if (!component) {
      console.warn("No component provided to addToContextMenu");
      return;
    }
    if (!event) {
      console.warn("No event provided to addToContextMenu");
      return;
    }

    // When exclusive, don't add to or update the menu
    if (menuState.isExclusive && menuState.isOpen) {
      return;
    }

    // Store the components and sources for this state's menu
    // These are updated by the event bubbling through the DOM
    components = [...components, component];
    sources = [...sources, ref.current];

    let position;
    if (anchorEl) {
      const { left, top, width, height } = anchorEl.current.getBoundingClientRect();
      position = {
        left, top, width, height
      }
    } else {
      position = {
        left: event.clientX,
        top: event.clientY,
        width: 0,
        height: 0
      }
    }
    // Exclusive menus set state immediately and then
    // stop the event from creating more menus
    if (isExclusive && !menuState.isExclusive) {
      event.stopPropagation();
      setMenuState({
        isOpen: true,
        position,
        sources: [ref.current],
        components: [component],
        onCloseFn: onClose,
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
      components,
      onCloseFn: onClose,
      isExclusive: menuState.isExclusive,
    });
  }, [menuState]);

  /**
   * Returns true when the menu is open
   * If a ref is passed, it will return true if the ref is open
   * @param ref?: React.MutableRefObject<HTMLElement>,
   * @returns boolean
   */
  const getIsMenuOpen = (ref?: React.MutableRefObject<HTMLElement>) => {
    if (!ref) return menuState?.isOpen;
    return menuState?.sources?.includes(ref.current);
  };

  return {
    addToContextMenu,
    getIsMenuOpen,
    onCloseMenu,
    contextMenuPosition: menuState.position,
    contextMenuSources: menuState.sources,
    isContextMenuOpen: menuState.isOpen,
    contextMenuComponents: menuState.components,
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
    contextMenuComponents,
    onCloseMenu,
  } = contextMenuState;

  const menuRef = React.useRef<HTMLDivElement>(null);

  // Close when using mousewheel outside of the menu
  React.useEffect(() => {
    if (isContextMenuOpen) {
      window.addEventListener("wheel", onCloseMenu);
      window.addEventListener("dblclick", onCloseMenu);
    }
    return () => {
      window.removeEventListener("wheel", onCloseMenu);
      window.removeEventListener("dblclick", onCloseMenu);
    }
  }, [isContextMenuOpen])

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
          <MenuList className="app-context-menu" ref={menuRef}>
            {contextMenuComponents.map((Child, index) => {
              if (typeof Child === "function") {
                return <Child key={index} />
              } else {
                return <React.Fragment key={index}>{Child}</React.Fragment>;
              }
            })}
          </MenuList>
        </Portal>
      </Menu>
    </ContextMenuContext.Provider>
  );
}