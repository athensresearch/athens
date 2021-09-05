import React from 'react';
import styled from 'styled-components';
import { DOMRoot } from '../../../../config';

import { AddCircle } from '@material-ui/icons';
import { Modal, Popper } from '@material-ui/core';

import { Button } from '../../../Button';
import { Overlay } from '../../../Overlay';
import { Badge } from '../../../Badge';
import { Menu } from '../../../Menu';

import { DBIcon } from './components/DBIcon';
import { DBMenuItem } from './components/DBMenuItem';

const DBMenuOverlay = styled(Overlay)`
  width: 16rem;
  max-height: 90vh;
  overflow-y: auto;

  @supports (overflow-y: overlay) {
    overflow-y: overlay;
  }
`;

const ActiveDb = styled.div`
  display: flex;
  padding: 0.25rem 1rem 0.25rem 0.5rem;
  gap: 0.25rem;
  overflow: hidden;

  svg {
    --size: 2rem;
    flex: 0 0 2.5rem;
    margin-left: -0.325rem;
    font-size: inherit;
  }

  main {
    display: flex;
    flex: 1 1 100%;
    /* Width is specified so that descendant text will get ellipses properly
    without resorting to overflow: hidden on this element. */
    width: calc(100% - 2rem);
    flex-direction: column;
    align-items: flex-start;
    justify-content: stretch;
  }

  h3 {
    margin: 0;
  }

  .tools {
    display: flex;
    margin-top: 0.5rem;
    font-size: var(--font-size--text-sm);

    button {
      padding: 0.25rem 0.5rem;
    }
  }
`;

const Name = styled.h4`
  font-weight: normal;
  font-size: var(--font-size--text-sm);
  color: inherit;
  margin: 0;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
`;

const Path = styled.span`
  display: block;
  color: var(--body-text-color---opacity-med);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 12px;
  white-space: nowrap;
`;

interface DBMenuProps {
  activeDb: Database
  inactiveDbs: Database[]
  synced: boolean
  handleChooseDb: (db: Database) => void
  handlePressAddDb: () => void
  handlePressRemoveDb: (db: Database) => void
  handlePressImportDb: (db: Database) => void
  handlePressMoveDb: (db: Database) => void
}

/**
 * Menu for switching to and joining graphs.
 */
export const DBMenu = ({
  handlePressAddDb,
  handleChooseDb,
  handlePressImportDb,
  handlePressRemoveDb,
  handlePressMoveDb,
  activeDb,
  inactiveDbs,
  synced
}: DBMenuProps) => {
  const [isMenuOpen, setIsMenuOpen] = React.useState(true);
  const [menuAnchor, setMenuAnchor] = React.useState(null);

  const handlePressDBMenu = (e) => {
    setMenuAnchor(e.currentTarget);
    setIsMenuOpen(true);
  };

  const handleCloseDBMenu = () => {
    setMenuAnchor(null);
    setIsMenuOpen(false);
  };

  return (
    <>
      <Button
        isPressed={isMenuOpen}
        onClick={handlePressDBMenu}
      >
        {synced ? (
          <Badge style={{}}><DBIcon name={activeDb.name} /></Badge>
        ) : (
          <DBIcon name={activeDb.name} />
        )}
      </Button>
      <Modal
        onClose={handleCloseDBMenu}
        BackdropProps={{ invisible: true }}
        container={DOMRoot}
        open={isMenuOpen}
      >
        <Popper
          open={isMenuOpen}
          anchorEl={menuAnchor}
          disablePortal={true}
          placement="bottom-start"
        >
          <DBMenuOverlay className="animate-in">
            <Menu>
              <ActiveDb>
                <DBIcon name={activeDb.name} />
                <main>
                  <Name>{activeDb.name}</Name>
                  <Path>{activeDb.id}</Path>
                  <div className="tools">
                    {activeDb["is-remote"] ? (
                      <>
                        <Button onClick={(activeDb) => handlePressImportDb(activeDb)}>Import</Button>
                        <Button onClick={(activeDb) => handlePressRemoveDb(activeDb)}>Remove</Button>
                      </>
                    ) : (
                      <>
                        <Button onClick={(activeDb) => handlePressMoveDb(activeDb)}>Import</Button>
                        <Button onClick={(activeDb) => handlePressRemoveDb(activeDb)}>Remove</Button>
                      </>
                    )}
                  </div>
                </main>
              </ActiveDb>
              {inactiveDbs.length > 0 && (
                <>
                  <Menu.Separator />
                  {inactiveDbs.map(db => <DBMenuItem
                    handleChooseDb={handleChooseDb}
                    db={db}
                    key={db.id} />)}
                </>
              )}
              <Menu.Separator />
              <Button onClick={handlePressAddDb}><AddCircle /><span>Add Database</span></Button>
            </Menu>
          </DBMenuOverlay>
        </Popper>
      </Modal>
    </>
  )
}