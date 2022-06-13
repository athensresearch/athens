import * as React from "react";
import {
  Menu,
  MenuButton,
  MenuProps,
  Placement,
  Portal,
  PortalProps,
  useControllableState,
  useOutsideClick
} from "@chakra-ui/react";

export type MenuSource = "cursor" | "box";
export type MenuTargetRect = { position?: "fixed" | "absolute", left?: number; top?: number; width?: number; height?: number };
export interface useContextMenuProps {
  ref: React.RefObject<HTMLDivElement>;
  source?: MenuSource;
  placement?: Placement;
  menuProps?: MenuProps;
  portalProps?: PortalProps;
}
export interface ContextMenuProps {
  children: React.ReactElement<any, string | React.JSXElementConstructor<any>>;
}

/**
 * Creates a context menu interaction on the target
 * @param ref The target element
 * @param source The visual source of the menu
 * @param placement The placement of the menu
 * @param menuProps The props to pass to the inner Menu component
 * @param portalProps The props to pass to the inner Portal component
 * @returns An object containing the props for the menu target, the menu component, and the state of the menu
 */
export const useContextMenu = ({
  ref,
  source,
  placement,
  menuProps,
  portalProps
}: useContextMenuProps) => {
  const menuTargetRect = React.useRef<MenuTargetRect | null>(null);
  const [isOpen, setIsOpen] = useControllableState({ defaultValue: false });
  const menuRef = React.useRef(null);

  useOutsideClick({
    ref: menuRef,
    handler: () => setIsOpen(false)
  });

  // ContextMenu event handler:
  // Prevent the default context menu event,
  // then set the target box according to the source rect,
  // then open the menu
  const handleContextMenu = (e: MouseEvent) => {
    if (e.target === ref.current || ref.current.contains(e.target as Node)) {
      e.preventDefault();
      e.stopPropagation();

      if (source === "cursor") {
        menuTargetRect.current = {
          position: "fixed",
          left: e.pageX,
          top: e.pageY,
        };
      } else {
        const box = ref.current.getBoundingClientRect();
        menuTargetRect.current = {
          position: "absolute",
          left: box.x,
          top: box.y,
          width: box.width,
          height: box.height
        };
      }
      setIsOpen(true);
    }
  };

  /**
   * A wrapper around Chakra's Menu with adjustments
   * for use as a context menu
   */
  const ContextMenu = ({ children }: ContextMenuProps) => {
    return isOpen ? (
      <Portal {...portalProps}>
        <Menu
          isOpen={true}
          placement={placement}
          closeOnBlur={true}
          onClose={() => setIsOpen(false)}
          {...menuProps}
        >
          <MenuButton
            style={{
              pointerEvents: "none",
              ...menuTargetRect.current,
            }}
          />
          {React.cloneElement(children, {
            ...children.props,
            ref: menuRef
          })}
        </Menu>
      </Portal>
    ) : null;
  };

  const menuSourceProps = {
    onContextMenu: handleContextMenu
  };

  return {
    menuSourceProps,
    ContextMenu,
    isOpen
  };
};

useContextMenu.defaultProps = {
  source: "cursor"
};
