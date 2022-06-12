import * as React from "react";
import {
  Menu,
  MenuButton,
  MenuProps,
  Placement,
  Portal,
  useControllableState,
  useOutsideClick
} from "@chakra-ui/react";

export type MenuSource = "cursor" | "box";
export type SourceBox = { x: number; y: number; width: number; height: number };
export interface useContextMenuProps {
  ref: React.RefObject<HTMLElement>;
  content: React.ReactElement<any, string | React.JSXElementConstructor<any>>;
  source?: MenuSource;
  placement?: Placement;
  menuProps?: MenuProps;
}

export const useContextMenu = ({
  ref,
  content,
  source,
  placement,
  menuProps
}: useContextMenuProps) => {
  const menuTargetPosition = React.useRef<SourceBox | null>(null);
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
  const handleContextMenu = (e) => {
    e.preventDefault();
    if (source === "cursor") {
      menuTargetPosition.current = {
        x: e.clientX,
        y: e.clientY,
        width: 0,
        height: 0
      };
    } else {
      const box = ref.current.getBoundingClientRect();
      menuTargetPosition.current = {
        x: box.x,
        y: box.y,
        width: box.width,
        height: box.height
      };
    }
    setIsOpen(true);
  };

  // The menu that will be returned
  const ContextMenu = () => {
    const rect = menuTargetPosition.current;

    return (
      <Menu
        isOpen={isOpen}
        placement={placement}
        closeOnBlur={true}
        onClose={() => setIsOpen(false)}
        {...menuProps}
      >
        <MenuButton
          style={{
            position: "absolute",
            pointerEvents: "none", // prevent the created box from receiving events
            left: rect.x,
            top: rect.y,
            width: rect.width,
            height: rect.height
          }}
        />
        <Portal>
          {React.cloneElement(content, {
            ...content.props,
            ref: menuRef
          })}
        </Portal>
      </Menu>
    );
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
