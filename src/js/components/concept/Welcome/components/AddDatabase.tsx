import React from "react";
import styled, { css, keyframes } from "styled-components";

import "emoji-picker-element";
import { HexColorPicker } from "react-colorful";
import { Popper, Modal } from "@material-ui/core";
import { Cancel, AddFolder, Folder } from "iconoir-react";

import { DOMRoot } from "@/utils/config";
import { DatabaseIcon } from "@/concept/DatabaseIcon";
import { Button } from "@/Button";
import { Overlay } from "@/Overlay";
import { useMenu } from "@/Menu/hooks/useMenu";
import { Input } from "@/Input";

import { Heading, Header, Actions, PageWrapper } from "../Welcome";

const pulseInputOutline = keyframes`
  from {
    box-shadow: 0 0 0 2px var(--link-color---opacity-med);
  } to {
    box-shadow: 0 0 0 2px var(--link-color);
  }
`;

const pulseColorCaret = keyframes`
  from {
    transform: translate(-50%, -50%) scale(1) ;
  } to {
    transform: translate(-50%, -50%) scale(1.08);
  }
`;

const DatabaseNameField = styled(Input)`
  text-align: center;
  border-radius: 100em;
  transition: all 0.2s ease-in-out;

  &:focus {
    outline: none;
    animation: ${pulseInputOutline} 1s ease-in-out alternate infinite;
  }
`;

const Preview = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  gap: 0.5rem;
  flex: 0 0 auto;
  margin-top: auto;

  ${DatabaseIcon.Wrap} {
    --size: 5em;
    border-radius: 18%;
    box-shadow: 0 0.5rem 1.5rem -0.5rem var(--shadow-color);
  }
`;

const EmojiPickerOverlay = styled(Overlay)`
  flex-direction: column;

  emoji-picker {
    --background: var(--background-plus-1);
    --border-color: var(--body-text-color---opacity-low);
    --indicator-color: var(--link-color);
    --input-border-color: transparent;
    --input-font-color: var(--body-text-color);
    --input-placeholder-color: var(--body-text-color---opacity-med);
    --outline-color: transparent;
    --category-font-color: var(--body-text-color);
    --button-active-background: var(--body-text-color---opacity-med);
    --button-hover-background: var(--body-text-color---opacity-10);
  }
`;

const PathInput = styled.input.attrs({
  type: "file",
  webkitdirectory: true,
  directory: true,
})`
  visibility: hidden;
  position: absolute;
`;

const PathPlaceholder = styled.span`
  opacity: 0.5;
  text-decoration: underline;
`;

const PathPicker = styled.label`
  display: grid;
  padding: 0.25rem 1rem;
  grid-template-areas: "main";
  place-content: center;
  place-items: center;
  cursor: pointer;
  transition: background 0.2s ease-in-out;

  &:hover {
    border-radius: 100em;
    background-color: var(--background-plus-2---opacity-med);
  }

  > svg {
    grid-area: main;
    margin: auto auto auto 0;
    pointer-events: none;
    font-size: var(--font-size--text-xs);
    color: var(--body-text-color---opacity-med);
  }

  span {
    padding-left: 1.5rem;
    grid-area: main;
    font-size: 0.8rem;
  }
`;

const Extras = styled.fieldset`
  display: flex;
  align-items: stretch;
  margin: 1rem auto auto;
  justify-content: center;
  gap: 1rem;
  padding: 1rem 1rem 0.5rem;
  background: var(--background-color);
  border: 1px solid var(--border-color);
  border-radius: 1rem;
`;

const IconButton = styled(Button)`
  height: 3rem;
  width: 3rem;
  border-radius: 0.5rem;
  padding: 0;
  place-content: center;
  place-items: center;
  background: var(--background-plus-1);
  border: 1px solid var(--border-color);
  font-size: 2em;
  will-change: transform;

  span {
    opacity: 0.5;
    flex: 0 0 auto;
    filter: grayscale(100%);
    transform: scale(0.9);
    transition: filter 0.1s ease-in-out, opacity 0.1s ease-in-out,
      transform 0.15s ease-in;
  }

  &:hover,
  &:focus,
  &.hasIcon {
    span {
      opacity: 1;
      filter: none;
      transform: scale(1);
    }
  }
`;

const Path = styled.span`
  font-size: 0.8rem;
  grid-area: main;
`;

const ColorPickerWrap = styled.div`
  flex: 1 1 100%;

  .react-colorful {
    width: 9rem;
    height: 100%;
    gap: 1rem;
    margin: 0;
    flex-direction: row;

    > * {
      border-radius: 0.5rem;
      height: 100%;
      flex: 0 0 4rem;
    }
  }

  .react-colorful__saturation {
    border-bottom: 0;
  }

  .react-colorful__interactive:focus .react-colorful__pointer {
    animation: ${pulseColorCaret} 0.5s infinite alternate ease-in-out;
  }
`;

const ControlWrap = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.125rem;
`;

const ControlLabel = styled.span`
  font-size: var(--font-size--text-xs);
  color: var(--body-text-color---opacity-high);
`;

const EmptyDatabase = {
  id: "",
  name: "New Database",
  isRemote: false,
  icon: "",
  color: "#006cef",
};

const RemoveEmojiButton = styled(Button)`
  justify-items: center;
  gap: 0;

  span {
    flex: 0 0 auto;
  }
`;

const Picker = ({ setDatabaseIcon, closeMenu }) => {
  const ref = React.useRef(null);

  React.useEffect(() => {
    ref.current.addEventListener("emoji-click", (event) => {
      setDatabaseIcon(event.detail.emoji.unicode);
      closeMenu();
    });
    ref.current.skinToneEmoji = "👍";
  }, []);

  React.useEffect(() => {
    const picker = document.getElementsByTagName("emoji-picker")[0];
    const style = document.createElement("style");
    style.textContent = css`
      .picker {
        border: 0;
      }
      input.search {
        border: 0;
        background: var(--background-color);
        font-family: var(--font-family-default);
        color: var(--body-text-color---opacity-high);
        padding-left: 0.5rem;
        border-radius: 0.25rem;
      }
      input.search:focus {
        border-radius: 0.25rem;
        outline: none;
      }

      .indicator {
        background-color: var(--link-color);
      }
    `;
    picker.shadowRoot.appendChild(style)!;
  }, []);

  return React.createElement("emoji-picker", { ref });
};

export interface AddDatabaseProps {
  onAddFromFile: (database: Database) => void;
  onCreateDatabase: (database: Database) => void;
  onGoBack: () => void;
}

export const AddDatabase = React.forwardRef(
  (props: AddDatabaseProps, ref): JSX.Element => {
    const {
      onAddFromFile: handleAddFromFile,
      onCreateDatabase: handleCreateDatabase,
      onGoBack: handleGoBack,
    } = props;
    const [database, setDatabase] = React.useState(EmptyDatabase);
    const { triggerProps, menuProps, closeMenu } = useMenu();

    const setDatabaseIcon = (emoji) =>
      setDatabase({
        ...database,
        icon: emoji,
      });

    const handleClearDatabaseIcon = () =>
      setDatabase({
        ...database,
        icon: null,
      });

    const handleChangeDatabaseColor = (color) => {
      setDatabase({
        ...database,
        color: color,
      });
    };

    const handleChangeDatabasePath = (e) => {
      const path = e.target.value;
      setDatabase({
        ...database,
        id: path,
      });
    };

    return (
      <PageWrapper ref={ref}>
        <Header>
          <Button onClick={(database) => handleAddFromFile(database)}>
            <AddFolder
              style={{ marginLeft: "0.125rem", marginRight: "0.125rem" }}
            />
            <span>Add From File</span>
          </Button>
        </Header>
        <Preview style={{ "--shadow-color": database.color }}>
          <Heading>Create a New Database</Heading>
          <DatabaseIcon {...database} />
          <DatabaseNameField
            placeholder={"Name"}
            defaultValue={database.name || EmptyDatabase.name}
            onChange={(e) =>
              setDatabase({ ...database, name: e.target.value.trim() })
            }
          />
          <PathPicker htmlFor="pathInput">
            <Folder />
            <PathInput
              id="pathInput"
              onChange={handleChangeDatabasePath}
              hidden
            />
            {database.id ? (
              <Path>{database.id}</Path>
            ) : (
              <PathPlaceholder>Choose a location</PathPlaceholder>
            )}
          </PathPicker>
          <Extras>
            <ControlWrap>
              <IconButton
                className={database.icon ? "hasIcon" : ""}
                aria-label="Choose an icon"
                {...triggerProps("click")}
              >
                <span>{database.icon ? database.icon : "📚️"}</span>
              </IconButton>
              <ControlLabel>Icon</ControlLabel>
            </ControlWrap>
            <ControlWrap>
              <ColorPickerWrap>
                <HexColorPicker
                  aria-label="Choose a color"
                  color={database.color}
                  onChange={handleChangeDatabaseColor}
                />
              </ColorPickerWrap>
              <ControlLabel>Color</ControlLabel>
            </ControlWrap>
          </Extras>
        </Preview>
        <Actions>
          <Button shape="round" variant="gray" onClick={handleGoBack}>
            Cancel
          </Button>
          <Button
            shape="round"
            variant="filled"
            onClick={() => handleCreateDatabase(database)}
          >
            Create
          </Button>
        </Actions>

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
              placement="right-start"
              modifiers={[
                {
                  name: "flip",
                  enabled: true,
                  options: {
                    altBoundary: true,
                    rootBoundary: "document",
                    padding: 8,
                  },
                },
              ]}
            >
              <EmojiPickerOverlay {...menuProps}>
                <Picker
                  setDatabaseIcon={setDatabaseIcon}
                  closeMenu={closeMenu}
                />
                <RemoveEmojiButton
                  onClick={() => {
                    handleClearDatabaseIcon(), closeMenu();
                  }}
                >
                  <Cancel />
                  <span>Clear</span>
                </RemoveEmojiButton>
              </EmojiPickerOverlay>
            </Popper>
          </Modal>
        )}
      </PageWrapper>
    );
  }
);
