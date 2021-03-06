/*
 * Copyright 2017-2020 EPAM Systems, Inc. (https://www.epam.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from 'react';
import PropTypes from 'prop-types';
import {inject, observer} from 'mobx-react';
import {CSV, buildCascadeComposers} from './composers';

const SEPARATOR = ',';

class ExportConsumer extends React.Component {
  componentDidMount () {
    const {export: exportStore} = this.props;
    exportStore.attach(this);
  }

  componentWillUnmount () {
    const {export: exportStore} = this.props;
    exportStore.detach(this);
  }

  getExportData = () => {
    const {composers} = this.props;
    const compose = buildCascadeComposers(composers);
    const csv = new CSV();
    return new Promise((resolve, reject) => {
      compose(csv)
        .then(() => {
          const content = csv.getData()
            .map(line => line.join(SEPARATOR))
            .join('\n');
          const blob = new Blob([content], {type: 'text/csv;charset=utf-8'});
          resolve(blob);
        })
        .catch(reject);
    });
  };

  render () {
    const {className, children} = this.props;
    return (
      <div className={className}>
        {children}
      </div>
    );
  }
}

ExportConsumer.propTypes = {
  className: PropTypes.string,
  composers: PropTypes.arrayOf(PropTypes.shape({
    composer: PropTypes.func,
    options: PropTypes.array
  }))
};

ExportConsumer.defaultProps = {
  composers: []
};

export default inject('export')(
  observer(ExportConsumer)
);
