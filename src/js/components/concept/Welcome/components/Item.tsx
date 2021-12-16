import React from "react";
import styled from "styled-components";

import { DOMRoot } from "@/utils/config";
import { Popper, Modal, PopperPlacementType } from "@material-ui/core";
import { OfflineBolt } from "@material-ui/icons";

import { Menu } from "@/Menu";
import { Overlay } from "@/Overlay";
import { Button } from "@/Button";
import { DatabaseIcon } from "@/concept/DatabaseIcon";
import { useMenu } from "@/Menu/hooks/useMenu";

const EditButton = styled(Button)`
  border-radius: 100em;
  padding: 0.5rem 1rem;
  font-size: var(--font-size--text-sm);
  opacity: 0;
  grid-area: main;
  margin-left: auto;
  margin: auto 0.25rem auto auto;
  justify-self: center;
  align-self: flex-end;
  position: relative;
  z-index: 2;

  &:focus-visible,
  &[aria-pressed="true"] {
    opacity: 1;
  }
`;

const ItemWrap = styled.li`
  align-self: stretch;
  display: flex;
  align-items: stretch;
  justify-content: stretch;
  display: grid;
  grid-template-areas: "main";

  &.is-current {
    background-color: var(--background-plus-2);
    border-radius: 0.5rem;
    box-shadow: 0 0.25rem 0.5rem var(--shadow-color---opacity-15);
  }

  &:hover {
    ${EditButton} {
      opacity: 1;
    }
  }
`;

const ItemButton = styled(Button)`
  grid-area: main;
  flex-direction: column;
  align-items: stretch;
  gap: 0;
  flex: 1 1 100%;
  display: grid;
  justify-items: stretch;
  text-align: left;
  grid-template-areas: "icon name status" "icon detail status";
  grid-template-columns: auto 1fr;
  gap: 0 0.125rem;
  border-radius: 0.5rem;
  position: relative;
  z-index: 1;
  padding-right: 4rem;

  &[aria-pressed="true"] {
    background: var(--background-minus-2);
  }

  ${DatabaseIcon.Wrap} {
    grid-area: icon;
    grid-row: 1 / -1;
    margin: auto;
  }
`;

const Name = styled.h3`
  margin: 0;
  display: flex;
  grid-area: name;
  justify-self: stretch;
  font-weight: normal;
  font-size: var(--font-size--text-base);
  overflow: hidden;
  color: inherit;
`;

const Detail = styled.span`
  grid-area: detail;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: var(--font-size--text-xs);
  opacity: var(--opacity-med);
  color: inherit;
  overflow: hidden;
`;

const Status = styled.span`
  align-self: center;
  grid-area: status;
  grid-row: 1 / -1;
`;

interface ItemProps {
  isCurrentDatabase?: boolean;
  db: Database;
  onChooseDatabase?: (db: Database) => void;
  onRemoveDatabase?: (db: Database) => void;
  onRenameDatabase?: (db: Database) => void;
  onDeleteDatabase?: (db: Database) => void;
}

export const Item = ({
  isCurrentDatabase = false,
  db,
  onChooseDatabase,
  onRemoveDatabase,
  onRenameDatabase,
  onDeleteDatabase,
}: ItemProps): React.ReactElement => {
  const { triggerProps, menuProps, closeMenu } = useMenu();

  return (
    <>
      <ItemWrap className={isCurrentDatabase ? "is-current" : ""}>
        <ItemButton
          {...triggerProps("contextMenu")}
          isPressed={
            triggerProps("contextMenu").isPressed ||
            triggerProps("click").isPressed
          }
          onClick={() => onChooseDatabase && onChooseDatabase(db)}
        >
          <DatabaseIcon {...db} size="2em" />
          <Name>{db.name}</Name>
          <Detail>{db.id}</Detail>
          {/* TODO: Tooltip or help text when trying to interact with an offline DB */}
          {db.status === "offline" && (
            <Status>
              <OfflineBolt />
            </Status>
          )}
        </ItemButton>
        <EditButton shape="unset" {...triggerProps("click")}>
          Edit
        </EditButton>
      </ItemWrap>
      {menuProps.isOpen && (
        <Modal
          open={menuProps.isOpen}
          BackdropProps={{ invisible: true }}
          onClose={closeMenu}
          container={DOMRoot}
        >
          <Popper
            open={true}
            disablePortal={true}
            anchorEl={menuProps.anchorEl}
            placement={menuProps.placement as PopperPlacementType}
          >
            <Overlay {...menuProps}>
              <Menu>
                {onChooseDatabase && (
                  <Menu.Button
                    disabled={db.status === "offline"}
                    onClick={() => {
                      onChooseDatabase(db);
                      closeMenu();
                    }}
                  >
                    Open
                  </Menu.Button>
                )}
                {onRemoveDatabase && (
                  <Menu.Button
                    onClick={() => {
                      onRemoveDatabase(db);
                      closeMenu();
                    }}
                  >
                    Remove
                  </Menu.Button>
                )}
                {onRenameDatabase && (
                  <Menu.Button
                    onClick={() => {
                      onRenameDatabase(db);
                      closeMenu();
                    }}
                  >
                    Rename
                  </Menu.Button>
                )}
                <Menu.Separator />
                {onDeleteDatabase && (
                  <Menu.Button
                    onClick={() => {
                      onDeleteDatabase(db);
                      closeMenu();
                    }}
                  >
                    Delete
                  </Menu.Button>
                )}
              </Menu>
            </Overlay>
          </Popper>
        </Modal>
      )}
    </>
  );
};
