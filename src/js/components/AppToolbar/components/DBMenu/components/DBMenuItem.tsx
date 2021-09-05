import styled from 'styled-components';

import { Link } from '@material-ui/icons';

import { Badge } from '../../../../Badge';
import { Button } from '../../../../Button';
import { DBIcon } from './DBIcon';

const MenuItem = styled(Button)`
  display: flex;
  padding: 0.25rem 1rem 0.25rem 0.5rem;
  gap: 0.25rem;
  overflow: hidden;

  > svg {
    flex: 0 0 2rem;
    font-size: inherit;
  }

  main {
    display: flex;
    flex: 1 1 100%;
    flex-direction: column;
    align-items: flex-start;
    justify-content: stretch;
    overflow: hidden;
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
  display: flex;
  color: var(--body-text-color---opacity-med);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 12px;
  white-space: nowrap;

  svg {
    margin-right: 0.25rem;
  }
`;

interface DBMenuItemProps {
  db: Database;
  handleChooseDb: (db: Database) => void;
}

export const DBMenuItem = ({
  db,
  handleChooseDb,
}: DBMenuItemProps) => <MenuItem
  onClick={(db) => handleChooseDb(db)}
>
    <DBIcon name={db.name} />
    <main>
      <Name>{db.name}</Name>
      <Path>{db["is-remote"] && <Link />} {db.id}</Path>
    </main>
  </MenuItem>