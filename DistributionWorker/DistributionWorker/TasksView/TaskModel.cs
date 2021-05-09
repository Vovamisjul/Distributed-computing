using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DistributionWorker.TasksView
{
    public class TaskModel : INotifyPropertyChanged
    {
        private string id;
        public string Id
        {
            get
            {
                return id;
            }
            set
            {
                id = value;
                OnPropertyChanged("Id");
            }

        }

        private string copyId;
        public string CopyId
        {
            get
            {
                return copyId;
            }
            set
            {
                copyId = value;
                OnPropertyChanged("CopyId");
            }

        }

        private string name;
        public string Name
        {
            get
            {
                return name;
            }
            set
            {
                name = value;
                OnPropertyChanged("Name");
            }

        }

        private string description;
        public string Description
        {
            get
            {
                return description;
            }
            set
            {
                description = value;
                OnPropertyChanged("Description");
            }

        }

        private string comment;
        public string Comment
        {
            get
            {
                return comment;
            }
            set
            {
                comment = value;
                OnPropertyChanged("Comment");
            }

        }

        private bool downloaded;
        public bool Downloaded
        {
            get
            {
                return downloaded;
            }
            set
            {
                downloaded = value;
                OnPropertyChanged("Downloaded");
            }

        }

        private string style;
        public string Style
        {
            get
            {
                return style;
            }
            set
            {
                style = value;
                OnPropertyChanged("Style");
            }

        }

        private string color;
        public string Color
        {
            get
            {
                return color;
            }
            set
            {
                color = value;
                OnPropertyChanged("Color");
            }

        }

        private string cursor;
        public string Cursor
        {
            get
            {
                return cursor;
            }
            set
            {
                cursor = value;
                OnPropertyChanged("Cursor");
            }

        }

        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged(string propertyName)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
